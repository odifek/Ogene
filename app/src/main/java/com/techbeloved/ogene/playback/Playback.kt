package com.techbeloved.ogene.playback

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import io.reactivex.Observable

interface Playback {
    fun prepare(mediaUri: String, ready: (Boolean) -> Unit)

    /**
     * The player implementation will take care of preparing and starting the playback whenever it is ready
     */
    fun playWhenReady(queueItem: MediaSessionCompat.QueueItem): Observable<PlaybackStatus>

    fun start(checkPlayerStatus: Boolean = true)

    fun pause()

    fun stop()

    fun duck()

    fun isPlaying(): Boolean

    fun duration(): Int

    fun position(): Int

    /**
     * Monitor the state of the playback
     */
    fun playbackStatus(): Observable<PlaybackStatus>

    fun isReady(): Boolean

}

sealed class PlaybackStatus {
    data class Playing(val duration: Int, val position: Int): PlaybackStatus()
    data class Paused(val duration: Int, val position: Int): PlaybackStatus()
    data class Ducked(val duration: Int, val position: Int) : PlaybackStatus()
    data class Error(val message: String, val recoverable: Boolean): PlaybackStatus()

    object Completed : PlaybackStatus()
    object Stopped : PlaybackStatus()

    data class Started(
        val metadata: MediaMetadataCompat,
        val duration: Int
    ) : PlaybackStatus()
}
