package com.techbeloved.ogene.musicbrowser


import android.content.ComponentName
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techbeloved.ogene.MusicService
import com.techbeloved.ogene.OgeneApp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
class MusicBrowserViewModelTest {
    private lateinit var subject: MusicBrowserViewModel

    private lateinit var application: OgeneApp


    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()

        subject = MusicBrowserViewModel(
            MediaSessionConnection(
                application,
                ComponentName(application.applicationContext, MusicService::class.java)
            )
        )



    }

    @Test
    fun getItemsInCategory() {
        val allMusicCategory = buildCategoryUri(CATEGORY_ALL_SONGS, 0)
        subject.getItemsInCategory(allMusicCategory).observeForever { items ->
            Timber.i("loaded items: %s", items.snapshot())
            assertThat(items.loadedCount, `is`(not(0)))
        }
    }
}