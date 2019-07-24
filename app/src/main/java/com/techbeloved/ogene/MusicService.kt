package com.techbeloved.ogene

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.techbeloved.ogene.musicbrowser.CATEGORY_ROOT
import com.techbeloved.ogene.playback.NOW_PLAYING_NOTIFICATION
import com.techbeloved.ogene.playback.PlaybackManager
import com.techbeloved.ogene.repo.MusicProvider
import com.techbeloved.ogene.schedulers.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject


private const val TAG = "MusicService"

class MusicService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var musicProvider: MusicProvider

    @Inject
    lateinit var playbackManager: PlaybackManager

    private val disposables = CompositeDisposable()

    private var isForegroundService = false


    private val connectionCallback: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                super.onConnected()
                Timber.i("Service connected")
            }
        }

    override fun onCreate() {
        super.onCreate()
        (application as OgeneApp).appComponent.inject(this)
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(baseContext, TAG).apply {

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

            setSessionToken(sessionToken)

            setSessionActivity(sessionActivityPendingIntent)

        }

        playbackManager.setService(WeakReference(this))
        playbackManager.setMediaSession(mediaSession!!)
    }

    fun enableForeground(notification: Notification) {
        if (!isForegroundService) {
            ContextCompat.startForegroundService(
                applicationContext,
                Intent(applicationContext, this.javaClass)
            )
            startForeground(NOW_PLAYING_NOTIFICATION, notification)
            isForegroundService = true
        }
    }

    fun disableForeground(stopService: Boolean) {
        if (isForegroundService) {
            stopForeground(false)
            isForegroundService = false
        }
        // If playback has ended, also stop the service.
        if (stopService) {
            stopSelf()
            mediaSession?.isActive = false
        }
    }


    override fun onLoadChildren(parentId: String, result: Result<List<MediaItem>>) {
        result.detach()
        Timber.i("loadChildren called with no options: %s", parentId)
        musicProvider.getMediaItemsForMediaId(parentId)
            .subscribeOn(schedulerProvider.io())
            .subscribe(
                { mediaItems -> result.sendResult(mediaItems) },
                { error -> Timber.w(error, "Error loading items for id: %s", parentId) }
            ).let { disposables.add(it) }

    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaItem>>,
        options: Bundle
    ) {
        result.detach()
        val pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE)
        val startPos = options.getInt(MediaBrowserCompat.EXTRA_PAGE) * pageSize

        musicProvider.getMediaItemsForMediaId(parentId, startPos, pageSize)
            .subscribeOn(schedulerProvider.io())
            .subscribe(
                { mediaItems -> result.sendResult(mediaItems) },
                { error -> Timber.w(error, "Error loading items for id: %s", parentId) }
            ).let { disposables.add(it) }
        Timber.i("loadChildren called with: %s", parentId)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(CATEGORY_ROOT, null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Called when a media button is clicked
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            this?.isActive = false
            this?.release()
        }
        playbackManager.onDestroy()
        if (!disposables.isDisposed) disposables.dispose()
    }
}





