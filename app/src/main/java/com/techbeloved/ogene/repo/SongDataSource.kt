package com.techbeloved.ogene.repo

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.paging.PositionalDataSource
import com.techbeloved.ogene.repo.models.Song

/**
 * This is subscribed to by the ui which renders songs page by page for efficiency
 */
class SongDataSource(private val mediaBrowser: MediaBrowserCompat): PositionalDataSource<Song>() {

    private var rootId: String? = null
    private var loadedPages: MutableSet<Int> = HashSet()

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Song>) {
        val pageIndex = getPageIndex(params)
        if (loadedPages.contains(pageIndex)) {
            callback.onResult(emptyList())
            return
        }

        val parentId = getParentId(params.startPosition)
        val extra = getRangeBundle(params)
        mediaBrowser.subscribe(parentId, extra, object : MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>,
                options: Bundle
            ) {
                loadedPages.add(pageIndex)
            }
        })
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Song>) {

    }

    private fun getPageIndex(params: LoadRangeParams) = params.startPosition / params.loadSize

    private fun getParentId(requestedStartPosition: Int): String {
        if (rootId == null) {
            rootId = mediaBrowser.root
        }
        return rootId + requestedStartPosition
    }

    private fun getRangeBundle(params: LoadRangeParams): Bundle {
        val extra = Bundle()
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, getPageIndex(params))
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.loadSize)
        return extra
    }

    private fun getInitialPageBundle(params: LoadInitialParams): Bundle {
        val extra = Bundle()
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.pageSize)
        return extra
    }

}