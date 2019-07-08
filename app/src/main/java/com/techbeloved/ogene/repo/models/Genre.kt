package com.techbeloved.ogene.repo.models

import android.database.Cursor
import android.provider.MediaStore

class Genre() {

    var name: String? = null
    var id: Long = 0

    constructor(cursor: Cursor): this() {
        id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Genres._ID))
        name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Genres.NAME))
    }

    companion object {
        fun genresQuery(): Query {
            return Query.Builder()
                .uri(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(null)
                .args(null)
                .sort(null)
                .build()
        }

        /**
         * A song can belong to more than one genre
         */
        fun genresForSong(songId: Long): Query {
            val uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", songId.toInt())
            return Query.Builder()
                .uri(uri)
                .projection(null)
                .selection(null)
                .args(null)
                .sort(null)
                .build()
        }
    }
}