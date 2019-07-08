package com.techbeloved.ogene

import com.techbeloved.ogene.musicbrowser.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test



class BrowsingHelperTest {
    private val root = "content://com.techbeloved.ogene/"

    @Test
    fun validCategoryTest() {
        // setup
        val valid1 = "content://com.techbeloved.ogene/albums/12"
        val valid2 = "content://com.techbeloved.ogene/albums/12/"

        assertThat(valid1.isValidCategoryUri(), `is`(true))
        assertThat(valid2.isValidCategoryUri(), `is`(true))

    }

    @Test
    fun invalidCategoryUriTest() {
        val invalidCategoryUri = "content://com.techbeloved.ogene/12"
        val invalid2 = "content://com.techbeloved.ogene/albums"
        val invalid3 = "content://com.techbeloved.ogene/albums/12/songs/12"
        val invalid4 = "content://com.techbeloved.ogene/asawdc/23"

        assertThat(invalidCategoryUri.isValidCategoryUri(), `is`(false))
        assertThat(invalid2.isValidCategoryUri(), `is`(false))
        assertThat(invalid3.isValidCategoryUri(), `is`(false))
        assertThat(invalid4.isValidCategoryUri(), `is`(false))


    }

    @Test
    fun validTopLevelCategory() {
        val topLevel1 = "content://com.techbeloved.ogene/albums"
        val topLevel2 = "content://com.techbeloved.ogene/artists"

        assertThat(topLevel1.isValidTopLevelCategory(), `is`(true))
        assertThat(topLevel2.isValidTopLevelCategory(), `is`(true))

    }

    @Test
    fun invalidTopLevelCategory() {
        val invalid = "content://com.techbeloved.ogene/albums/12/songs/12"
        assertThat(invalid.isValidTopLevelCategory(), `is`(false))
    }

    @Test
    fun extractValidCategoryFromMediaUri() {
        val albumsLabel = "albums"
        val artistsLabel = "artists"

        val albumsCategory = "content://com.techbeloved.ogene/albums"
        val albumUri = "content://com.techbeloved.ogene/albums/12"
        val songUri = "content://com.techbeloved.ogene/albums/12/songs/1"
        val subcategory = "content://com.techbeloved.ogene/artists/12/albums"
        val subCategoryItem = "content://com.techbeloved.ogene/artists/12/albums/9"

        val invalidCategory = "content://com.techbeloved.ogene/albumsad"

        assertThat(albumsCategory.category(), `is`(albumsLabel))
        assertThat(albumUri.category(), `is`(albumsLabel))
        assertThat(songUri.category(), `is`(albumsLabel))
        assertThat(subcategory.category(), `is`(artistsLabel))
        assertThat(subCategoryItem.category(), `is`(artistsLabel))
        assertThat(invalidCategory.category(), `is`(nullValue()))
    }


    @Test
    fun extractCategoryIdFromMediaUri() {
        val albumUri = "content://com.techbeloved.ogene/albums/12"
        val songUri = "content://com.techbeloved.ogene/albums/12/songs/1"
        val subcategory = "content://com.techbeloved.ogene/artists/12/albums"
        val subCategoryItem = "content://com.techbeloved.ogene/artists/12/albums/9"

        assertThat(albumUri.categoryId(), `is`(12L))
        assertThat(songUri.categoryId(), `is`(12L))
        assertThat(subcategory.categoryId(), `is`(12L))
        assertThat(subCategoryItem.categoryId(), `is`(12L))
    }

    @Test
    fun extractSongIdFromUri() {
        val songUri = "content://com.techbeloved.ogene/albums/12/songs/1"
        val songInSubCategoryItem = "content://com.techbeloved.ogene/artists/12/albums/9/songs/2"
        val invalidSongUri = "content://com.techbeloved.ogene/albums/12"

        assertThat(songUri.songId(), `is`(1L))
        assertThat(songInSubCategoryItem.songId(), `is`(2L))
        assertThat(invalidSongUri.songId(), `is`(nullValue()))
    }

    @Test
    fun extractParentCategoryUriFromMediaUri() {
        val songUri = "content://com.techbeloved.ogene/albums/12/songs/1"
        val songInSubCategoryItem = "content://com.techbeloved.ogene/artists/12/albums/9/songs/2"

        assertThat(songUri.parentCategoryUri(), `is`("content://com.techbeloved.ogene/albums/12"))
        assertThat(songInSubCategoryItem.parentCategoryUri(), `is`("content://com.techbeloved.ogene/artists/12/albums/9"))
        assertThat(songInSubCategoryItem.parentCategoryUri()?.parentCategoryUri(), `is`("content://com.techbeloved.ogene/artists/12"))
    }

    @Test
    fun extractSubCategoryFromMediaUri() {
        val subCategoryItem = "content://com.techbeloved.ogene/artists/12/albums/9"
        val subCategoryItems = "content://com.techbeloved.ogene/artists/12/albums"
        val songInSubCategoryItem = "content://com.techbeloved.ogene/artists/12/albums/9/songs/2"
        val invalidSubCategoryItem = "content://com.techbeloved.ogene/artists/12"

        assertThat(subCategoryItem.subCategory(), `is`("albums"))
        assertThat(subCategoryItems.subCategory(), `is`("albums"))
        assertThat(songInSubCategoryItem.subCategory(), `is`("albums"))
        assertThat(invalidSubCategoryItem.subCategory(), `is`(nullValue()))

    }

    @Test
    fun extractSubCategoryIdFromMediaUri() {
        val subCategoryItem = "content://com.techbeloved.ogene/artists/12/albums/9"
        val songInSubCategoryItem = "content://com.techbeloved.ogene/artists/12/albums/9/songs/2"
        val invalidSubCategoryItem = "content://com.techbeloved.ogene/artists/12"

        assertThat(subCategoryItem.subCategoryId(), `is`(9L))
        assertThat(songInSubCategoryItem.subCategoryId(), `is`(9L))
        assertThat(invalidSubCategoryItem.subCategoryId(), `is`(nullValue()))

    }
}