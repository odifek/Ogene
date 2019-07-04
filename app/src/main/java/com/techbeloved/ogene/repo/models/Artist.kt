package com.techbeloved.ogene.repo.models

import android.database.Cursor
import android.provider.MediaStore

class Artist() {
    var id: Long = 0
    var title: String? = null
    var numberOfAlbums: Int = 0
    var numberOfTracks: Int = 0

    constructor(cursor: Cursor): this() {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
        numberOfAlbums = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
        numberOfTracks = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))

    }

    companion object {

        fun allArtistsQuery(): Query {
            return Query.Builder()
                .uri(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(null)
                .args(null)
                .sort(null)
                .build()
        }

        fun singleArtistQuery(artistId: Long): Query {
            val selection = "${MediaStore.Audio.Artists._ID}=$artistId"
            return Query.Builder()
                .uri(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(selection)
                .args(null)
                .sort(null)
                .build()
        }

    }

}