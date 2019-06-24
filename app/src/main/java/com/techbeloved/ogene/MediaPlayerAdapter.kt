package com.techbeloved.ogene

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import java.io.IOException

class MediaPlayerAdapter(
    private val context: Context,
    val sessionCallback: MediaPlayback.Callback
) : MediaPlayback {

    private var player: MediaPlayer? = null

    private fun initMediaPlayer() {
        player = player ?: MediaPlayer()

        // TODO: set on completion listener, buffering listener
        player?.setOnCompletionListener { mp -> sessionCallback.onPlaybackCompleted() }
    }

    override fun onPlayFromMediaId(mediaId: String) {
        try {
            val assetFileDescriptor: AssetFileDescriptor =
                context.resources.openRawResourceFd(mediaId.toInt()) ?: return

            try {
                initMediaPlayer()
                player?.setDataSource(
                    assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.length
                )
            } catch (e: IllegalStateException) {
                player?.release()
            }

            assetFileDescriptor.close()
            initMediaSessionMetaData()
        } catch (e: IOException) {
            return
        }

        try {
            player?.setOnPreparedListener { start() }
            player?.prepareAsync()
        } catch (e: IOException) {
            Log.e("Player", "Error preparing resource for playback", e)
        }
    }

    override fun start() {
        if (player != null && !isPlaying()) {
            player?.start()
            updatePlayerState(PlaybackStateCompat.STATE_PLAYING)
        }
        player?.setVolume(1.0f, 1.0f)
    }

    override fun stop() {
        if (player != null && isPlaying()) {
            player?.stop()
            updatePlayerState(PlaybackStateCompat.STATE_STOPPED)
        }
    }

    override fun pause() {
        if (player != null && isPlaying()) {
            player?.pause()
            updatePlayerState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    override fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    override fun duck() {
        player?.setVolume(0.3f, 0.3f)
    }

    private fun initMediaSessionMetaData() {

    }

    private fun updatePlayerState(@PlaybackStateCompat.State state: Int) {


    }

}

interface MediaPlayback {
    fun start()
    fun stop()
    fun pause()
    fun isPlaying(): Boolean
    fun onPlayFromMediaId(mediaId: String)
    fun duck()

    interface Callback {
        fun onPlaybackCompleted()
    }
}
