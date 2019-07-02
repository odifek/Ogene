package com.techbeloved.ogene.musicbrowser

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSessionConnection @Inject constructor (
    context: Context, serviceComponent: ComponentName
) {

    val isConnected = MutableLiveData<Boolean>()
        .apply { postValue(false) }

    val rootMediaId: String get() = mediaBrowser.root

    val browser: MediaBrowserCompat get() = mediaBrowser

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(context, serviceComponent,
        mediaBrowserConnectionCallback, null)

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
           isConnected.postValue(true)
        }

        override fun onConnectionSuspended() {
            isConnected.postValue(false)
        }

        override fun onConnectionFailed() {
            isConnected.postValue(false)
        }
    }
}