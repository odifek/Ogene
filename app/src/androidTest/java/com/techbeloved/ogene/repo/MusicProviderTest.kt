package com.techbeloved.ogene.repo

import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.provider.ProviderTestRule
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule

class MusicProviderAndroidTest {

    private lateinit var musicProvider: MusicProvider

    private lateinit var songsRepository: SongsRepository

    private lateinit var albumsRepository: AlbumsRepository

    private lateinit var artistsRepository: ArtistsRepository

    private lateinit var genresRepository: GenresRepository

    private lateinit var mediaContentResolver: MediaStoreContentResolver

    @Before
    fun setUp() {

        mediaContentResolver = MediaStoreContentResolver(ApplicationProvider.getApplicationContext())
        albumsRepository = AlbumsRepository(mediaContentResolver)
        songsRepository = SongsRepositoryImp(mediaContentResolver)
        artistsRepository = ArtistsRepository(mediaContentResolver)
        genresRepository = GenresRepository(mediaContentResolver)

        musicProvider = MusicProvider(songsRepository, albumsRepository, artistsRepository, genresRepository)

    }

    @Test
    fun getMediaItemsForMediaId() {

    }
}