package com.techbeloved.ogene

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.techbeloved.ogene.databinding.ActivityMainBinding
import com.techbeloved.ogene.musicbrowser.CATEGORY_ALL_SONGS
import com.techbeloved.ogene.musicbrowser.MediaListAdapter
import com.techbeloved.ogene.musicbrowser.MusicBrowserViewModel
import com.techbeloved.ogene.musicbrowser.buildCategoryUri
import com.vanniktech.rxpermission.Permission
import com.vanniktech.rxpermission.RealRxPermission
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val rxPermission by lazy { RealRxPermission.getInstance(applicationContext) }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        (application as OgeneApp).appComponent.inject(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        rxPermission.request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe({ permission ->
                if (permission.state() == Permission.State.GRANTED) {
                    val mediaListAdapter = MediaListAdapter { view, mediaItemModel ->
                        Timber.i("Item clicked: %s", mediaItemModel)
                    }

                    val viewModel: MusicBrowserViewModel =
                        ViewModelProviders.of(this, viewModelFactory)[MusicBrowserViewModel::class.java]

                    binding.recyclerviewSongList.apply {
                        adapter = mediaListAdapter
                        layoutManager = LinearLayoutManager(this@MainActivity)
                    }

                    val allSongsCategory = buildCategoryUri(CATEGORY_ALL_SONGS, 0)
                    viewModel.connected.observe(this, Observer { connected ->
                        if (connected) {
                            viewModel.getItemsInCategory(allSongsCategory)
                        }
                    })

                    viewModel.musicItems.observe(this, Observer { mediaItems ->
                        Timber.i("Received items: %s", mediaItems.size)
                        mediaListAdapter.submitList(mediaItems)
                    })

                } else {
                    Toast.makeText(this, "You need to grant read external disk permission", Toast.LENGTH_SHORT).show()
                }
            }, { Timber.i(it) })
            .let { disposables.add(it) }


    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposables.isDisposed) disposables.dispose()
    }
}
