package com.techbeloved.ogene.playback

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.techbeloved.ogene.repo.models.NowPlayingItem
import io.reactivex.Observable
import io.reactivex.Single

interface QueueManager {
    /**
     * Prepares the media queue and returns it in an observable
     */
    fun prepareFromMediaId(
        mediaId: String,
        @PlaybackStateCompat.ShuffleMode shuffleMode: Int
    ): Observable<MutableList<MediaSessionCompat.QueueItem>>

    /**
     * Get next on the queue without skipping
     */
    @Throws(EndOfQueueException::class)
    fun nextItem(): MediaSessionCompat.QueueItem

    /**
     * Get the previous item without skipping
     */
    @Throws(EndOfQueueException::class)
    fun previousItem(): MediaSessionCompat.QueueItem

    /**
     * Get the currently selected item
     */
    @Throws(QueueManagerNotReadyException::class)
    fun currentItem(): MediaSessionCompat.QueueItem

    /**
     * Skip to particular item position
     */
    @Throws(IllegalArgumentException::class)
    fun skipToItem(index: Long): MediaSessionCompat.QueueItem

    /**
     * Skip to next item if possible or returns null. Current item position is updated
     */
    @Throws(EndOfQueueException::class)
    fun skipToNextItem(): MediaSessionCompat.QueueItem

    /**
     * Skips to previous item if possible or returns null. Current item position is only updated if skipping is successful
     */
    @Throws(EndOfQueueException::class)
    fun skipToPrevious(): MediaSessionCompat.QueueItem

    /**
     * Restores the queue saved during the previous media playback session.
     * Song items on the queue are saved as ids and the current playback position is also saved.
     * The player is configured using these data so that playback could be resumed with no hassle
     */
    fun restoreSavedQueue(): Single<SavedQueue>
    /**
     * Saves the current playing item
     */
    fun saveCurrentItem(nowPlayingItem: NowPlayingItem)
}