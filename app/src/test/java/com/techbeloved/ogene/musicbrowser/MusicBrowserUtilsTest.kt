package com.techbeloved.ogene.musicbrowser

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicBrowserUtilsTest {

    private lateinit var subject: MusicBrowserUtils

    @Before
    fun setUp() {
        subject = MusicBrowserUtils()
    }

    @Test
    fun mediaType_verify_album_detail_uri() {
        val validAlbumItemUri = Uri.Builder()
            .authority(MusicBrowserUtils.AUTHORITY)
            .scheme(MusicBrowserUtils.SCHEME)
            .appendPath("albums")
            .appendPath("123")
            .build()
        val result = subject.mediaType(validAlbumItemUri)
        assertThat(result, `is`(instanceOf(MediaType.Browsable.AlbumDetails::class.java)))
    }

    @Test
    fun mediaType_verify_song_in_album_uri() {
        val validSonInAlbumItemUri = Uri.Builder()
            .authority(MusicBrowserUtils.AUTHORITY)
            .scheme(MusicBrowserUtils.SCHEME)
            .appendPath("albums")
            .appendPath("123")
            .appendPath("songs")
            .appendPath("12")
            .build()
        val result = subject.mediaType(validSonInAlbumItemUri)
        assertThat(result, `is`(instanceOf(MediaType.Playable.SongInAlbum::class.java)))
    }

    @Test
    fun mediaType_verify_song_in_album_by_artist_uri() {
        val validUri = Uri.Builder()
            .authority(MusicBrowserUtils.AUTHORITY)
            .scheme(MusicBrowserUtils.SCHEME)
            .appendPath("artists")
            .appendPath("1")
            .appendPath("albums")
            .appendPath("12")
            .appendPath("songs")
            .appendPath("123")
            .build()

        val result = subject.mediaType(validUri)
        assertThat(result, `is`(instanceOf(MediaType.Playable.SongInAlbumByArtist::class.java)))
    }
}