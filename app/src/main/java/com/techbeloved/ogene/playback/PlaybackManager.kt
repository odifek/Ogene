package com.techbeloved.ogene.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.core.app.NotificationManagerCompat
import com.jakewharton.rxrelay2.PublishRelay
import com.techbeloved.ogene.MusicService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * PlaybackManager is the central class that handles all media player related requests.
 * [setMediaSession] must be called with active [MediaSessionCompat] before any thing happens here
 */
class PlaybackManager @Inject constructor(
    private val context: Context,
    private val notificationBuilder: NotificationBuilder,
    private val playback: Playback,
    private val queueManager: QueueManager
) {

    private var currentShuffleMode: Int = SHUFFLE_MODE_NONE
    private var mediaSessionCallbacks: MediaSessionCompat.Callback

    private lateinit var mediaSession: MediaSessionCompat

    private lateinit var mediaController: MediaControllerCompat

    private lateinit var serviceRef: WeakReference<MusicService>

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(
            context
        )
    }

    private val playbackStateBuilder: Builder by lazy { Builder() }

    private val becomingNoisyReceiver: BecomingNoisyReceiver by lazy {
        BecomingNoisyReceiver(context, mediaSession.sessionToken)
    }

    private val disposables = CompositeDisposable()

    private val mediaControllerCallback: MediaControllerCallback by lazy {
        MediaControllerCallback()
    }

    init {
        mediaSessionCallbacks = MediaSessionCallback()

    }

    /**
     * Set the media session. This must be called as soon as the media session is ready
     */
    fun setMediaSession(mediaSession: MediaSessionCompat) {
        this.mediaSession = mediaSession
        this.mediaSession.setCallback(mediaSessionCallbacks)

        mediaController = MediaControllerCompat(context, mediaSession).also {
            it.registerCallback(mediaControllerCallback)
        }

    }

    /**
     *
     */
    fun setService(service: WeakReference<MusicService>) {
        serviceRef = service
    }

    private fun updateNotification(state: PlaybackStateCompat) {
        val updatedState = state.state

        val notification = if (mediaSession.controller.metadata != null
            && updatedState != STATE_NONE
        ) {
            notificationBuilder.buildNotification(mediaSession)
        } else {
            null
        }

        when (updatedState) {
            STATE_BUFFERING,
            STATE_PLAYING -> {
                becomingNoisyReceiver.register()

                if (notification != null) {
                    notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)

                    serviceRef.get()?.enableForeground(notification)
                }
            }
            else -> {
                becomingNoisyReceiver.unregister()
                val shouldStopService = (updatedState == STATE_NONE)
                        || (updatedState == STATE_STOPPED)
                serviceRef.get()?.disableForeground(shouldStopService)
                if (notification != null) {
                    notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                } else {
                    serviceRef.get()?.stopForeground(true)
                }
            }
        }
    }

    fun onDestroy() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        mediaController.unregisterCallback(mediaControllerCallback)
    }

    private fun setPlaybackState(state: Int, position: Long = 0L, extras: Bundle? = null) {
        val playbackRate = 1.0f // TODO
        val updateTime = SystemClock.elapsedRealtime()
        when (state) {
            STATE_PLAYING -> {
                playbackStateBuilder.setActions(
                    ACTION_PLAY_PAUSE
                            or ACTION_PAUSE
                            or ACTION_SKIP_TO_NEXT
                            or ACTION_SKIP_TO_PREVIOUS
                )
                playbackStateBuilder.setState(state, position, playbackRate, updateTime)
                extras?.let { playbackStateBuilder.setExtras(it) }
            }
            STATE_PAUSED -> {
                playbackStateBuilder.setActions(
                    ACTION_PLAY_PAUSE
                            or ACTION_PLAY
                            or ACTION_SKIP_TO_NEXT
                            or ACTION_SKIP_TO_PREVIOUS
                )
                playbackStateBuilder.setState(state, position, playbackRate)
            }
            else -> {
                playbackStateBuilder.setState(
                    state,
                    PLAYBACK_POSITION_UNKNOWN,
                    0.0f
                )
            }
        }
        val playbackState = playbackStateBuilder.build()
        mediaSession.setPlaybackState(playbackState)
        updateNotification(playbackState)
    }

    private fun setMetaData(metadata: MediaMetadataCompat) {
        mediaSession.setMetadata(metadata)
    }


    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        private lateinit var audioFocusRequest: AudioFocusRequest

        private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        private var shouldPlayOnFocusGain: Boolean = false

        private val playFromMediaIdSubject = PublishRelay.create<String>()

        /**
         * Process queue items as they
         */
        private val queueSubject = PublishRelay.create<MediaSessionCompat.QueueItem>()

        /**
         * Receives and processes playback status events from the media player implementation
         */
        private val playbackConsumer: Consumer<PlaybackStatus> = Consumer { status ->
            when (status) {
                is PlaybackStatus.Started -> {
                    setMetaData(status.metadata)
                }
                is PlaybackStatus.Playing -> {
                    val extras = Bundle().apply {
                        putLong(EXTRA_MEDIA_DURATION, status.duration.toLong())
                        putLong(EXTRA_MEDIA_POSITION, status.position.toLong())
                    }
                    setPlaybackState(STATE_PLAYING, status.position.toLong(), extras = extras)
                }
                is PlaybackStatus.Paused -> {
                    val extras = Bundle().apply {
                        putLong(EXTRA_MEDIA_DURATION, status.duration.toLong())
                        putLong(EXTRA_MEDIA_POSITION, status.position.toLong())
                    }
                    setPlaybackState(STATE_PAUSED, status.position.toLong(), extras)
                }
                is PlaybackStatus.Ducked -> {
                }
                is PlaybackStatus.Error -> {
                }
                PlaybackStatus.Completed -> {
                    setPlaybackState(STATE_PAUSED)
                    onSkipToNext()
                }
                PlaybackStatus.Stopped -> {
                    setPlaybackState(STATE_STOPPED)
                }
            }
        }

        private val errorConsumer: Consumer<Throwable> = Consumer { throwable ->
            Timber.w(throwable)
        }


        init {
            playFromMediaIdSubject.switchMap { mediaId ->
                queueManager.prepareFromMediaId(mediaId, currentShuffleMode)
            }.map { queueItems ->
                mediaSession.setQueue(queueItems)
                mediaSession.setQueueTitle("Currently Playing")
                queueManager.currentItem()
            }
                .subscribe({ item -> queueSubject.accept(item) }, { Timber.w(it) })
                .let { disposables.add(it) }

            playback.playbackStatus()
                .subscribe(playbackConsumer, errorConsumer)
                .let { disposables.add(it) }

            queueSubject.switchMap { queueItem ->
                playback.playWhenReady(queueItem)
            }
                .subscribe(playbackConsumer, errorConsumer)
                .let { disposables.add(it) }
        }


        override fun onSkipToPrevious() {
            try {
                queueSubject.accept(queueManager.skipToPrevious())
            } catch (e: EndOfQueueException) {
                Timber.w(e, "Cannot skip previous!")
            }
        }


        override fun onPlay() {
            if (playback.isReady()) {
                if (getAudioFocus()) {
                    mediaSession.isActive = true
                    playback.start(true)

                }
            }
        }

        override fun onStop() {
            discardAudioFocus()
            playback.stop()
        }

        override fun onSkipToQueueItem(id: Long) {
            try {
                queueSubject.accept(queueManager.skipToItem(id))
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Invalid queue id, %s", id)
            }
        }

        override fun onSkipToNext() {
            try {
                queueSubject.accept(queueManager.skipToNextItem())
            } catch (e: EndOfQueueException) {
                Timber.w(e, "Cannot skip previous!")
            }
        }


        override fun onPause() {
            playback.pause()
            discardAudioFocus()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            if (mediaId != null) {
                playFromMediaIdSubject.accept(mediaId)
            }
        }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            super.onPlayFromSearch(query, extras)
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            super.onPlayFromUri(uri, extras)
        }


        private fun discardAudioFocus() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
            } else {
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
        }

        private fun getAudioFocus(): Boolean {
            val result: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setOnAudioFocusChangeListener(audioFocusChangeListener)
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    build()
                }

                result = audioManager.requestAudioFocus(audioFocusRequest)
            } else {
                // DONE: request audio focus for lower android versions
                result = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }

            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        private val audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener =
            AudioManager.OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        playback.pause()
                        shouldPlayOnFocusGain = false
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        if (playback.isPlaying()) {
                            playback.pause()
                            shouldPlayOnFocusGain = true // We want to resume later
                        } else {
                            shouldPlayOnFocusGain = false
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        if (playback.isPlaying()) {
                            playback.duck()
                            shouldPlayOnFocusGain = true
                        }
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        if (shouldPlayOnFocusGain) {
                            playback.start()
                        }
                    }
                }
            }
    }

    private inner class MediaControllerCallback :
        MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            //mediaController.playbackState?.let { updateNotification(it) }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            //mediaController.playbackState?.let { updateNotification(it) }
        }

    }

    private class BecomingNoisyReceiver(
        private val context: Context,
        sessionToken: MediaSessionCompat.Token
    ) : BroadcastReceiver() {

        private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private val controller = MediaControllerCompat(context, sessionToken)

        private var registered = false

        fun register() {
            if (!registered) {
                context.registerReceiver(this, noisyIntentFilter)
                registered = true
            }
        }

        fun unregister() {
            if (registered) {
                context.unregisterReceiver(this)
                registered = false
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                controller.transportControls.pause()
            }
        }
    }
}

const val EXTRA_MEDIA_DURATION = "mediaDuration"
const val EXTRA_MEDIA_POSITION = "mediaPosition"