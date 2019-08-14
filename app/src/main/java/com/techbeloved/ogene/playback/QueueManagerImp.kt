package com.techbeloved.ogene.playback

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.techbeloved.ogene.musicbrowser.isValidSongUri
import com.techbeloved.ogene.musicbrowser.parentCategoryUri
import com.techbeloved.ogene.musicbrowser.songId
import com.techbeloved.ogene.repo.MusicProvider
import com.techbeloved.ogene.repo.models.NowPlayingItem
import io.reactivex.Maybe
import io.reactivex.Observable
import javax.inject.Inject

class QueueManagerImp @Inject constructor(private val musicProvider: MusicProvider) : QueueManager {
    private val masterList: MutableList<MediaSessionCompat.QueueItem> = mutableListOf()
    private val currentList: MutableList<MediaSessionCompat.QueueItem> = mutableListOf()
    private val shuffledList: MutableList<MediaSessionCompat.QueueItem> = mutableListOf()

    private var currentItemPosition: Long = 0

    private var queueManagerReady: Boolean = true
    private var currentBrowsingMediaId: String? = null
    private var currentSongId: Long? = null

    @Synchronized
    override fun prepareFromMediaId(mediaId: String, @PlaybackStateCompat.ShuffleMode shuffleMode: Int):
            Observable<MutableList<MediaSessionCompat.QueueItem>> {

        if (!queueManagerReady) {
            return Observable.error(
                QueueManagerNotReadyException(
                    "Queue Manager is not ready to handle this request"
                )
            )
        } else if (!mediaId.isValidSongUri()) {
            return Observable.error(
                InvalidMediaIdException(
                    "Supplied media id, $mediaId, is invalid or not recognized!"
                )
            )
        } else {
            currentBrowsingMediaId = mediaId.parentCategoryUri()
            if (currentBrowsingMediaId == null) {
                return Observable.error(InvalidMediaIdException(
                    "Supplied media id, $mediaId, is invalid or not recognized!"
                ))
            }
            currentSongId = mediaId.songId()
            return musicProvider.getMediaItemsForMediaId(currentBrowsingMediaId!!)
                .map { mediaItems ->


                    val children = mediaItems.toQueueItems()
                    masterList.clear()
                    masterList.addAll(children)

                    shuffledList.clear()
                    shuffledList.addAll(masterList.shuffled())

                    currentList.clear()
                    when (shuffleMode) {
                        PlaybackStateCompat.SHUFFLE_MODE_NONE -> currentList.addAll(
                            masterList
                        )
                        else -> currentList.addAll(shuffledList)
                    }
                    currentItemPosition =
                        currentList.indexOfFirst { item -> item.description.mediaId == mediaId }
                            .toLong()

                    // Save the queue
                    musicProvider.saveQueueItems(
                        currentList.map { item -> item.description.mediaId?.songId()!!.toString() }

                    )

                    children.toMutableList()

                }.toObservable()
        }
    }


    override fun nextItem(): MediaSessionCompat.QueueItem {
        return currentList.getOrElse(currentItemPosition.toInt() + 1) {
            throw EndOfQueueException("End of queue reached! or queue manager not ready")
        }
    }


    override fun previousItem(): MediaSessionCompat.QueueItem {
        return currentList.getOrElse(currentItemPosition.toInt() - 1) {
            throw EndOfQueueException("Reached queue end! No more previous item")
        }
    }

    override fun currentItem(): MediaSessionCompat.QueueItem {
        return currentList.getOrElse(currentItemPosition.toInt()) {
            throw QueueManagerNotReadyException("Queue is empty or current item not set")
        }
    }

    override fun skipToItem(index: Long): MediaSessionCompat.QueueItem {
        require(index >= 0 && index < currentList.size)
        currentItemPosition = index
        return currentList[index.toInt()]
    }

    override fun skipToNextItem(): MediaSessionCompat.QueueItem {
        val nextItem = nextItem()
        currentItemPosition++
        return nextItem
    }

    override fun skipToPrevious(): MediaSessionCompat.QueueItem {
        val previousItem = previousItem()
        currentItemPosition--
        return previousItem
    }

    override fun restoreSavedQueue(): Maybe<SavedQueue> {
        return musicProvider.getSavedQueueItems()
            .filter { items -> items.isNotEmpty() } // Only continue if there is something in the list
            .flatMap { savedItems ->
                musicProvider.getCurrentItem()
                    .filter { item -> item.id > 0 }
                    .map { item -> SavedQueue(savedItems.toQueueItems(), item) }
                    .map { savedQueue ->

                        // Current list should be populated as is
                        currentList.addAll(savedQueue.queueItems)

                        masterList.clear()
                        masterList.addAll(savedQueue.queueItems)

                        shuffledList.clear()
                        shuffledList.addAll(masterList.shuffled())

                        if (savedQueue.currentItem.id > 0 && currentList.isNotEmpty()) {
                            currentItemPosition =
                                currentList.indexOfFirst { item -> item.description.mediaId?.songId() == savedQueue.currentItem.id }
                                    .toLong()
                        }

                        savedQueue
                    }
            }
    }

    override fun saveCurrentItem(nowPlayingItem: NowPlayingItem) {
        musicProvider.saveCurrentItem(nowPlayingItem)
    }
}

/**
 * Converts a list of [MediaBrowserCompat.MediaItem]s to [MediaSessionCompat.QueueItem]s
 */
private fun List<MediaBrowserCompat.MediaItem>.toQueueItems(startIndex: Int = 0): List<MediaSessionCompat.QueueItem> {
    return this
        .filter { mediaItem -> mediaItem.isPlayable }
        .mapIndexed { index, mediaItem ->
            MediaSessionCompat.QueueItem(mediaItem.description, startIndex + index.toLong())
        }
}


