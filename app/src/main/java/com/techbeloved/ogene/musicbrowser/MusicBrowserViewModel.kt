package com.techbeloved.ogene.musicbrowser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.techbeloved.ogene.musicbrowser.models.MediaItemModel
import com.techbeloved.ogene.schedulers.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class MusicBrowserViewModel @Inject constructor(
    private val mediaSessionConnection: MediaSessionConnection,
    private val musicProvider: MusicProvider,
    private val schedulerProvider: SchedulerProvider
) : ViewModel() {

    val connected: LiveData<Boolean> = mediaSessionConnection.isConnected

    private val itemsLiveData: MutableLiveData<List<MediaItemModel>> = MutableLiveData()
    val musicItems: LiveData<List<MediaItemModel>> get() = itemsLiveData

    private val disposables = CompositeDisposable()

    fun getItemsInCategory(parentId: String) {
        musicProvider.loadSongs(parentId, mediaSessionConnection.browser).subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe({ items ->
                itemsLiveData.value = items
            }, { Timber.w(it) })
            .let { disposables.add(it) }
    }

    override fun onCleared() {
        super.onCleared()
        if (!disposables.isDisposed) disposables.dispose()
    }
}