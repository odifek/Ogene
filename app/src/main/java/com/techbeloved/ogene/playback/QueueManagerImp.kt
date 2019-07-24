package com.techbeloved.ogene.playback

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.techbeloved.ogene.MusicService
import com.techbeloved.ogene.musicbrowser.isValidSongUri
import com.techbeloved.ogene.musicbrowser.parentCategoryUri
import com.techbeloved.ogene.musicbrowser.songId
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class QueueManagerImp @Inject constructor(context: Context) : QueueManager {
    private val masterList: MutableList<MediaSessionCompat.QueueItem> = mutableListOf()
    private val currentList: MutableList<MediaSessionCompat.QueueItem> = mutableListOf()
    private val shuffledList: MutableList<MediaSessionCompat.QueueItem> = mutableListOf()

    private var currentItemPosition: Long = 0

    private var queueManagerReady: Boolean = true
    private var currentMediaId: String? = null
    private var currentSongId: Long? = null

    private var connected = false

    private val connectionCallback: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                super.onConnected()
                connected = true
                Timber.i("Connection success")
            }

            override fun onConnectionFailed() {
                super.onConnectionFailed()
                connected = false
                Timber.i("Connection failed")
            }

            override fun onConnectionSuspended() {
                super.onConnectionSuspended()
                connected = false
                Timber.i("Suspended")
            }
        }

    private val mediaBrowser: MediaBrowserCompat

    init {
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MusicService::class.java), connectionCallback, null
        ).apply {
            connect()
        }
    }

    @Synchronized
    override fun prepareFromMediaId(mediaId: String, @PlaybackStateCompat.ShuffleMode shuffleMode: Int): Observable<MutableList<MediaSessionCompat.QueueItem>> {
        return Observable.create { emitter ->
            if (!queueManagerReady) {
                emitter.tryOnError(
                    QueueManagerNotReadyException(
                        "Queue Manager is not ready to handle this request"
                    )
                )
            } else if (!mediaBrowser.isConnected) {
                mediaBrowser.connect()
                emitter.tryOnError(
                    ServiceNotReadyException(
                        "Music service not ready or not yet connected!"
                    )
                )
            } else if (!mediaId.isValidSongUri()) {
                emitter.tryOnError(
                    InvalidMediaIdException(
                        "Supplied media id, $mediaId, is invalid or not recognized!"
                    )
                )
            } else {
                currentMediaId?.let { mediaBrowser.unsubscribe(it) }
                currentMediaId = mediaId.parentCategoryUri()
                currentSongId = mediaId.songId()

                currentMediaId?.let {
                    mediaBrowser.subscribe(it, object : MediaBrowserCompat.SubscriptionCallback() {
                        override fun onChildrenLoaded(
                            parentId: String,
                            children: MutableList<MediaBrowserCompat.MediaItem>
                        ) {
                            masterList.clear()
                            masterList.addAll(children.toQueueItems())

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
                            emitter.onNext(currentList)
                        }

                        override fun onError(parentId: String) {
                            Timber.w("Error subscribing to mediaId, %s", parentId)
                            emitter.tryOnError(InvalidMediaIdException("Error subscribing to mediaId, $parentId"))
                        }

                    })
                }
            }
            emitter.setCancellable { mediaBrowser.unsubscribe(mediaId) }
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
}

/**
 * Converts a list of [MediaBrowserCompat.MediaItem]s to [MediaSessionCompat.QueueItem]s
 */
private fun List<MediaBrowserCompat.MediaItem>.toQueueItems(startIndex: Int = 0): List<MediaSessionCompat.QueueItem> {
    return this.mapIndexed { index, mediaItem ->
        MediaSessionCompat.QueueItem(mediaItem.description, startIndex + index.toLong())
    }
}


