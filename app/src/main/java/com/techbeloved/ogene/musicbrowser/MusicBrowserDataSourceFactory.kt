package com.techbeloved.ogene.musicbrowser

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.techbeloved.ogene.musicbrowser.models.MediaItemModel
import com.techbeloved.ogene.musicbrowser.models.SimpleMediaItem

class MusicBrowserDataSourceFactory(
    private val mediaBrowser: MediaBrowserCompat,
    private val parentId: String
) : DataSource.Factory<Int, MediaItemModel>() {
    override fun create(): DataSource<Int, MediaItemModel> {
        return MusicBrowserDataSource(mediaBrowser, parentId)
    }

}
/**
 * Provides a data source for browsing the given parentId
 * @param mediaBrowser is the [MediaBrowserCompat] client to use to subscribe to the parent mediaId
 * @param parentId the parent or category mediaId which we want to list the children
 */
class MusicBrowserDataSource(
    private val mediaBrowser: MediaBrowserCompat,
    private val parentId: String
) : PositionalDataSource<MediaItemModel>() {

    private val loadedPages: MutableSet<Int> = HashSet()

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<MediaItemModel>) {
        val pageIndex = getPageIndex(params)
        if (loadedPages.contains(pageIndex)) {
            callback.onResult(emptyList())
            return
        }

        val extra = getRangeBundle(params)
        mediaBrowser.subscribe(parentId, extra, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>,
                options: Bundle
            ) {
                loadedPages += pageIndex
                val itemModels = children.map(mediaItemToModelMapper)
                callback.onResult(itemModels)
            }
        })
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<MediaItemModel>) {
        val extra: Bundle = getInitialPageBundle(params)
        mediaBrowser.subscribe(parentId, extra, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>,
                options: Bundle
            ) {
                loadedPages += 0
                val itemModels = children.map(mediaItemToModelMapper)
                callback.onResult(itemModels, params.requestedStartPosition)
            }
        })
    }

    private fun getPageIndex(params: LoadRangeParams) = params.startPosition / params.loadSize

    private fun getRangeBundle(params: LoadRangeParams): Bundle {
        val extra = Bundle()
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, getPageIndex(params))
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.loadSize)
        return extra
    }

    private fun getInitialPageBundle(params: LoadInitialParams): Bundle {
        val extra = Bundle()
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0)
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.requestedLoadSize)
        return extra
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
}

private val String?.itemId: Long?
    get() = this?.substringAfterLast("/")?.toLongOrNull()