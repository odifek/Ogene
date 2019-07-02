package com.techbeloved.ogene.musicbrowser

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.techbeloved.ogene.musicbrowser.models.MediaItemModel
import javax.inject.Inject

class MusicBrowserViewModel @Inject constructor (private val mediaSessionConnection: MediaSessionConnection): ViewModel() {

    fun getItemsInCategory(parentId: String): LiveData<PagedList<MediaItemModel>> {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .build()
        return LivePagedListBuilder<Int, MediaItemModel>(MusicBrowserDataSourceFactory(mediaSessionConnection.browser, parentId), config)
            .build()
    }
}