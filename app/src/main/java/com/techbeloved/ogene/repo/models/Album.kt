package com.techbeloved.ogene.repo.models

import android.database.Cursor
import android.provider.MediaStore

class Album() {
    var id: Long = 0
    var title: String? = null
    var albumArtUri: String? = null
    var albumArtist: String? = null
    var year: Long = 2000
    var numberOfSongs: Int = 0

    constructor(cursor: Cursor) : this() {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID))
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
        albumArtUri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART))
        albumArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST))
        year = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.LAST_YEAR))
        numberOfSongs = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
    }

    companion object {
        fun allAlbumsQuery(sortBy: SortBy): Query {

            return Query.Builder()
                .uri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(null)
                .args(null)
                .sort(getSortOrder(sortBy))
                .build()
        }

        fun singleAlbumQuery(id: String): Query {
            val selection = "(${MediaStore.Audio.Albums.ALBUM_ID}=?)"
            return Query.Builder()
                .uri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(selection)
                .args(listOf(id))
                .sort(getSortOrder())
                .build()
        }

        fun albumsByArtistQuery(artistId: Long): Query {
            val uri = MediaStore.Audio.Artists.Albums.getContentUri("external", artistId)
            return Query.Builder()
                .uri(uri)
                .projection(null)
                .selection(null)
                .args(null)
                .sort(getSortOrder())
                .build()
        }

        private fun getSortOrder(sortBy: SortBy = SortBy.ALBUM): String {
            return when (sortBy) {
                SortBy.ALBUM -> MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
                SortBy.YEAR -> MediaStore.Audio.Albums.LAST_YEAR + " DESC"
                SortBy.YEAR_REVERSE -> MediaStore.Audio.Albums.LAST_YEAR + " ASC"
                SortBy.ARTIST_NAME -> MediaStore.Audio.Albums.ARTIST + " ASC"
            }
        }
    }

    enum class SortBy {
        ALBUM, YEAR, YEAR_REVERSE, ARTIST_NAME
    }

}

