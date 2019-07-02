package com.techbeloved.ogene.musicbrowser

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import com.techbeloved.ogene.musicbrowser.models.MediaItemModel
import com.techbeloved.ogene.musicbrowser.models.SimpleMediaItem
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicProvider @Inject constructor() {

    fun loadSongs(parentId: String, mediaBrowser: MediaBrowserCompat): Observable<List<MediaItemModel>> {
        return Observable.create { emitter ->
            mediaBrowser.subscribe(parentId, object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {

                    val itemModels = children.map(mediaItemToModelMapper)
                    Timber.i("Updating ui with items: %s", itemModels.size)
                    if (!emitter.isDisposed) {
                        emitter.onNext(itemModels)
                    }
                }
            })
        }
    }

    val mediaItemToModelMapper: (MediaBrowserCompat.MediaItem) -> MediaItemModel = { mediaItem ->
        SimpleMediaItem(
            id = mediaItem.mediaId.itemId,
            mediaId = mediaItem.mediaId,
            title = mediaItem.description.title.toString(),
            subtitle = mediaItem.description.subtitle?.toString(),
            description = mediaItem.description.description?.toString()
        )
    }

    private val String?.itemId: Long?
        get() = this?.substringAfterLast("/")?.toLongOrNull()
}