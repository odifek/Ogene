package com.techbeloved.ogene.repo

import android.content.Context
import android.database.Cursor
import androidx.core.content.ContentResolverCompat
import com.techbeloved.ogene.repo.models.Song

class CursorBasedSongsRepository(applicationContext: Context): SongsRepository {

    private var cursor: Cursor = ContentResolverCompat.query(
        applicationContext.contentResolver,
        Song.allSongsQuery.uri,
        Song.allSongsQuery.projection,
        Song.allSongsQuery.selection,
        Song.allSongsQuery.args,
        Song.allSongsQuery.sort,
        null
    )

    override fun numberOfSongs(): Int = cursor.count

    override fun getSongAtPosition(position: Int): Song? {
        if (!cursor.moveToPosition(position)) return null
        return Song(cursor)
    }

    override fun getSongsAtRange(startPosition: Int, endPosition: Int): List<Song> {
        return ArrayList<Song>().apply {
            for (position in startPosition until endPosition) {
                getSongAtPosition(position)?.let { add(it) }
            }
        }
    }

    override fun getAllSongs(): List<Song> {
        return ArrayList<Song>().apply {
            while (cursor.moveToNext()) add(Song(cursor))
        }
    }
}