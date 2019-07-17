package com.techbeloved.ogene.playback

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import io.reactivex.Observable

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
    fun nextItem(): MediaSessionCompat.QueueItem

    /**
     * Get the previous item without skipping
     */
    fun previousItem(): MediaSessionCompat.QueueItem

    /**
     * Get the currently selected item
     */
    fun currentItem(): MediaSessionCompat.QueueItem

    /**
     * Skip to particular item position
     */
    fun skipToItem(index: Long): MediaSessionCompat.QueueItem

    /**
     * Skip to next item if possible or returns null. Current item position is updated
     */
    fun skipToNextItem(): MediaSessionCompat.QueueItem

    /**
     * Skips to previous item if possible or returns null. Current item position is only updated if skipping is successful
     */
    fun skipToPrevious(): MediaSessionCompat.QueueItem
}