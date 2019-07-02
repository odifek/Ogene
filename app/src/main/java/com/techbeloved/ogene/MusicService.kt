package com.techbeloved.ogene

import android.app.PendingIntent
import android.content.*
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.techbeloved.ogene.repo.SongsRepository
import com.techbeloved.ogene.repo.extensions.mediaItem
import com.techbeloved.ogene.schedulers.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

private const val MEDIA_ROOT_ID = "__ROOT__"
private const val EMPTY_MEDIA_ROOT_ID = "__EMPTY__"
private const val TAG = "MusicService"

private const val CHANNEL_ID = "musicChannelId"
private const val NOTIFICATION_ID = 12

class MusicService : MediaBrowserServiceCompat(), MediaPlayback.Callback {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    private val disposables = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()


        (application as OgeneApp).appComponent.inject(this)

        player = MediaPlayerAdapter(this, this)

        val mediaButtonReceiver = ComponentName(applicationContext, MediaButtonReceiver::class.java)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this@MusicService, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this@MusicService, 0, mediaButtonIntent, 0)

        mediaSession = MediaSessionCompat(this, TAG, mediaButtonReceiver, pendingIntent).apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                        or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)

            setPlaybackState(stateBuilder.build())

            setCallback(MusicSessionCallback(applicationContext))

            setMediaButtonReceiver(pendingIntent)

            setSessionToken(sessionToken)

        }
    }


    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        result.detach()
        Timber.i("loadChildren called with no options: %s", parentId)

        songsRepository.getAllSongs().subscribeOn(schedulerProvider.io()).subscribe(
            { songs ->
                songs.map { it.mediaItem(parentId) }.let { result.sendResult(it.toMutableList()) }
            },
            { error -> Timber.w(error) }
        ).let { disposables.add(it) }

    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>, options: Bundle) {
        result.detach()
        val pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE)
        val startPos = options.getInt(MediaBrowserCompat.EXTRA_PAGE) * pageSize
        val endPos = startPos + pageSize

        songsRepository.getSongsAtRange(startPos, endPos).subscribeOn(schedulerProvider.io()).subscribe(
            { songs ->
                songs.map { it.mediaItem(parentId) }.let { result.sendResult(it.toMutableList()) }
            },
            { error -> Timber.w(error) }
        ).let { disposables.add(it) }

        Timber.i("loadChildren called with: %s", parentId)

    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Called when a media button is clicked
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showPlayingNotification() {
        val builder = MediaStyleHelper.from(this, mediaSession)

        builder.addAction(
            NotificationCompat.Action(
                R.drawable.ic_pause,
                getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)
            )
        )
        val notification = builder.build()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun showPausedNotification() {
        val builder = MediaStyleHelper.from(this, mediaSession)

        builder.addAction(
            NotificationCompat.Action(
                R.drawable.ic_play,
                getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
        )
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build())
        stopForeground(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            this?.isActive = false
            this?.release()
        }
        if (!disposables.isDisposed) disposables.dispose()
    }

    // region Media Player callbacks

    override fun onPlaybackCompleted() {

    }
    // endregion Media player callbacks

    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    // Defined elsewhere
    private val noisyAudioStreamReceiver = BecomingNoisyReceiver()
    private lateinit var player: MediaPlayback

    inner class MusicSessionCallback(
        private val context: Context
    ) : MediaSessionCompat.Callback() {
        // TODO: implement the callbacks
        private lateinit var audioFocusRequest: AudioFocusRequest

        private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        private val playbackStateBuilder = PlaybackStateCompat.Builder()

        private var shouldPlayOnFocusGain: Boolean = false

        private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener =
            AudioManager.OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        if (player.isPlaying()) {
                            player.pause()
                            shouldPlayOnFocusGain = false
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        if (player.isPlaying()) {
                            player.pause()
                            shouldPlayOnFocusGain = true
                        } else {
                            shouldPlayOnFocusGain = false
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        player.duck()
                        shouldPlayOnFocusGain = true
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        if (!player.isPlaying() && shouldPlayOnFocusGain) {
                            player.start()
                        }
                    }
                }
            }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            mediaId?.let { player.onPlayFromMediaId(it) }
        }


        override fun onPlay() {
            // Request audio focus for playback

            if (getAudioFocus()) {
                // Start service
                startService(Intent(context, MusicService::class.java))
                mediaSession?.isActive = true
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                // start the player
                player.start()
                // Register BECOMING_NOISY BroadcastReceiver
                registerReceiver(noisyAudioStreamReceiver, intentFilter)
                // Put the service in the foreground, post notification
                showPlayingNotification()
            }

            // You need to call startService here to ensure the service does not die when unbound
        }

        private fun setMediaPlaybackState(state: Int) {
            when (state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    playbackStateBuilder.setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                                or PlaybackStateCompat.ACTION_PAUSE
                    )
                    playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    playbackStateBuilder.setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                                or PlaybackStateCompat.ACTION_PLAY
                    )
                    playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
                }
            }

            mediaSession?.setPlaybackState(playbackStateBuilder.build())
        }

        override fun onStop() {
            // TODO: Update metadata and state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
            } else {
                // TODO: Abandon the normal focus for lower android versions
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
            unregisterReceiver(noisyAudioStreamReceiver)
            // Stop the service
            stopSelf()
            // Set the session inactive (and update metadata and state)
            mediaSession?.isActive = false
            // stop player
            player.stop()
            showPausedNotification()
        }

        override fun onPause() {
            // TODO: Update metadata and state

            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)

            player.pause()
            // Unregister noisy receiver
            unregisterReceiver(noisyAudioStreamReceiver)
            // Take service out of foreground, retain notfication
            showPausedNotification()
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
                // TODO: request audio focus for lower android versions
                result = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }

            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    inner class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Pause the player if it is playing
            if (player.isPlaying()) player.pause()
        }

    }


}

object MediaStyleHelper {
    /**
     * Build a notification using the information from the given media session. Makes heavy use
     * of [MediaMetadataCompat.getDescription] to extract the appropriate information.
     * @param context Context used to construct the notification.
     * @param mediaSession Media session to get information.
     * @return A pre-built notification with information from the given media session.
     */
    fun from(
        context: Context, mediaSession: MediaSessionCompat?
    ): NotificationCompat.Builder {
        val controller = mediaSession?.controller
        val mediaMetadata = controller?.metadata
        val description = mediaMetadata?.description

        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            // Add the metadata for the currently playing track
            setContentTitle(description?.title)
            setContentText(description?.subtitle)
            setSubText(description?.description)
            description?.iconBitmap?.let { setLargeIcon(it) }

            // Enable launching the player by clicking the notification
            controller?.sessionActivity?.let { setContentIntent(it) }

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)
            )

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            setSmallIcon(R.drawable.ic_play_circle_filled)
            color = ContextCompat.getColor(context, R.color.colorPrimaryDark)

            // Take advantage of MediaStyle features
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0)

                    // Add cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }

    }
}


