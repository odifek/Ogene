package com.techbeloved.ogene.repo

import com.techbeloved.ogene.repo.models.Artist
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistsRepository @Inject constructor(
    private val storeContentResolver: MediaStoreContentResolver
) {
    /**
     * Retrieves list of all artists
     */
    fun getAllArtists(
        offset: Int = 0,
        limit: Int = -1
    ): Observable<List<Artist>> {
        val query = Artist.allArtistsQuery()
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor.mapToList(offset, limit) { Artist(it) }
            }
    }

    /**
     * Retrieves details of a single artist
     */
    fun getArtist(artistId: Long): Observable<Artist> {
        val query = Artist.singleArtistQuery(artistId)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery -> mediaQuery.run().mapToOne { Artist(it) } }
    }
}