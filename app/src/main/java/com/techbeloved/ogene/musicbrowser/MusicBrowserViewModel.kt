package com.techbeloved.ogene.musicbrowser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.techbeloved.ogene.musicbrowser.models.MediaItemModel
import com.techbeloved.ogene.schedulers.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class MusicBrowserViewModel @Inject constructor(
    private val mediaSessionConnection: MediaSessionConnection
) : ViewModel() {

    val connected: LiveData<Boolean> = mediaSessionConnection.isConnected

    /**
     * Queries for and returns items for the given parentId. Since we want to subscribe to the parentId,
     * a MusicDataSource is created for each parentId
     * @param parentId is a browsing id of the form "content://com.techbeloved.ogene/albums/12"
     *  which represents album of id 12
     * @return [PagedList] [LiveData] which is used for paging of content
     */
    fun getItemsInCategory(parentId: String): LiveData<PagedList<MediaItemModel>> {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(20)
            .build()
        return LivePagedListBuilder(MusicBrowserDataSourceFactory(mediaSessionConnection.browser, parentId), config)
            .build()
    }
}