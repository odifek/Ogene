package com.techbeloved.ogene.repo

import com.techbeloved.ogene.repo.models.Album
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumsRepository @Inject constructor(
    private val storeContentResolver: MediaStoreContentResolver
) {

    fun getAlbums(
        offset: Int = 0,
        limit: Int = -1,
        sortBy: Album.SortBy = Album.SortBy.ALBUM
    ): Observable<List<Album>> {
        val query = Album.allAlbumsQuery(sortBy)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor.mapToList(offset, limit) { Album(it) }
            }

    }

    /**
     * Given an artist Id, retrieves all albums in which the artist featured
     */
    fun getAlbumsForArtist(
        artistId: Long,
        offset: Int = 0,
        limit: Int = -1
    ): Observable<List<Album>> {

        val query = Album.albumsByArtistQuery(artistId)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor.mapToList(offset, limit) { Album(it) }
            }
    }

    fun getAlbumById(albumId: Long): Observable<Album> {
        val query = Album.singleAlbumQuery(albumId.toString())
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor?.mapToOne { Album(it) }
            }
    }

}

