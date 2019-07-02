package com.techbeloved.ogene.repo

import android.content.Context
import android.database.Cursor
import androidx.core.content.ContentResolverCompat
import com.techbeloved.ogene.repo.models.Song
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CursorBasedSongsRepository @Inject constructor(applicationContext: Context) : SongsRepository {


    private val cursor: Cursor? by lazy {

        ContentResolverCompat.query(
            applicationContext.contentResolver,
            Song.allSongsQuery.uri,
            Song.allSongsQuery.projection,
            Song.allSongsQuery.selection,
            Song.allSongsQuery.args,
            Song.allSongsQuery.sort,
            null
        )
    }

    override fun numberOfSongs(): Int = cursor?.count ?: 0

    override fun getSongAtPosition(position: Int): Song? {
        if (cursor?.moveToPosition(position) != true) return null
        return Song(cursor!!)
    }

    override fun getSongsAtRange(startPosition: Int, endPosition: Int): Single<List<Song>> {

        return Single.just<List<Song>>(
            ArrayList<Song>().apply {
                for (position in startPosition until endPosition) {
                    getSongAtPosition(position)?.let { add(it) }
                }
            }
        )

    }

    override fun getAllSongs(): Single<List<Song>> {
        return Single.just(ArrayList<Song>().apply {
            while (cursor?.moveToNext() == true) add(Song(cursor!!))
        })
    }
}