package com.techbeloved.ogene.repo

import android.support.v4.media.MediaBrowserCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.whenever
import com.techbeloved.ogene.repo.models.Song
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class MusicProviderTest {

    private lateinit var musicProvider: MusicProvider
    @Mock
    private lateinit var songsRepository: SongsRepository
    @Mock
    private lateinit var albumsRepository: AlbumsRepository
    @Mock
    private lateinit var artistsRepository: ArtistsRepository
    @Mock
    private lateinit var genresRepository: GenresRepository


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        musicProvider = MusicProvider(
            songsRepository, albumsRepository, artistsRepository, genresRepository
        )
    }

    @Test
    fun getMediaItemsForMediaId() {
        // Setup
        val songs = listOf(
            Song().apply {
                id = 1
                title = "song1"
                this.albumId = 12
            },
            Song().apply {
                id = 2
                title = "song2"
                this.albumId = 12
            }
        )
        whenever(songsRepository.getSongsForAlbum(anyLong())).thenReturn(Observable.just(songs))
        val parentId = "content://com.techbeloved.ogene/albums/12/"
        val mediaItemsObserver = TestObserver<List<MediaBrowserCompat.MediaItem>>()

        // Execute
        musicProvider.getMediaItemsForMediaId(parentId, 0, -1).subscribe(mediaItemsObserver)

        mediaItemsObserver.assertSubscribed()
        mediaItemsObserver.assertValueCount(1)
        mediaItemsObserver.assertNoErrors()
        mediaItemsObserver.assertValue { items -> items.size == 2 }

    }
}