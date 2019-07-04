package com.techbeloved.ogene.repo

import com.techbeloved.ogene.repo.models.Genre
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenresRepository @Inject constructor(private val storeContentResolver: MediaStoreContentResolver) {

    fun getAllGenres(offset: Int, limit: Int): Observable<List<Genre>> {
        val query = Genre.genresQuery()
        return storeContentResolver.createQuery(query)
            .map { mediaQuery -> mediaQuery.run().mapToList(offset, limit) { Genre(it) } }
    }

    fun getGenresForSong(songId: Long): Observable<List<Genre>> {
        val query = Genre.genresForSong(songId)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery -> mediaQuery.run().mapToList { Genre(it) } }
    }

}