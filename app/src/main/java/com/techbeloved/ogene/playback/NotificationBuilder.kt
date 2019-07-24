package com.techbeloved.ogene.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.techbeloved.ogene.BuildConfig
import com.techbeloved.ogene.R
import com.techbeloved.ogene.repo.extensions.isPlayEnabled
import javax.inject.Inject

/**
 * Takes care of displaying building notification using information from the [MediaSessionCompat]
 */
class NotificationBuilder @Inject constructor(private val context: Context) {

    private val platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val playAction = NotificationCompat.Action(
        R.drawable.ic_play,
        context.getString(R.string.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_PLAY)
    )
    private val pauseAction = NotificationCompat.Action(
        R.drawable.ic_pause,
        context.getString(R.string.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_PAUSE)
    )

    private val skipNextAction = NotificationCompat.Action(
        R.drawable.ic_skip_next,
        context.getString(R.string.next),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_SKIP_TO_NEXT)
    )

    private val skipPreviousAction = NotificationCompat.Action(
        R.drawable.ic_skip_previous,
        context.getString(R.string.previous),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_SKIP_TO_PREVIOUS)
    )

    private val stopPendingIntent =
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_STOP)

    fun buildNotification(mediaSession: MediaSessionCompat): Notification {
        if (shouldCreateNowPlayingChannel()) {
            createNotificationChannel()
        }

        val controller = mediaSession.controller
        val description = controller.metadata.description
        val playbackState = controller.playbackState

        val builder = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)

        val playPauseIndex = 1
        builder.addAction(skipPreviousAction)
        if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        } else {
            builder.addAction(pauseAction)
        }
        builder.addAction(skipNextAction)

        val mediaStyle = MediaStyle(builder)
            .setCancelButtonIntent(stopPendingIntent)
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(playPauseIndex)
            .setShowCancelButton(true)

        return builder.setContentIntent(controller.sessionActivity)
            .setContentText(description.subtitle)
            .setContentTitle(description.title)
            .setDeleteIntent(stopPendingIntent)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.ic_play)
            .setStyle(mediaStyle)
            .setLargeIcon(description.iconBitmap)
            .build()

    }


    private fun shouldCreateNowPlayingChannel() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists() =
        platformNotificationManager.getNotificationChannel(DEFAULT_CHANNEL_ID) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(DEFAULT_CHANNEL_ID,
            context.getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_LOW)
            .apply {
                description = context.getString(R.string.notification_channel_description)
            }
        platformNotificationManager.createNotificationChannel(notificationChannel)
    }

}

const val NOW_PLAYING_NOTIFICATION = 1212
const val DEFAULT_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".playback_channel"
