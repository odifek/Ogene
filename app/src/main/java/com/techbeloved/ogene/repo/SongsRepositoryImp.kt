package com.techbeloved.ogene.repo

import com.techbeloved.ogene.repo.models.Song
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongsRepositoryImp @Inject constructor(
    private val storeContentResolver: MediaStoreContentResolver
) : SongsRepository {
    override fun getSongsForAlbum(
        albumId: Long,
        offset: Int,
        limit: Int,
        sortBy: Song.SortBy
    ): Observable<List<Song>> {
        val query = Song.songsInAlbumQuery(albumId, sortBy)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor.mapToList(offset, limit) { Song(it) }
            }
    }

    override fun getSongsForArtist(
        artistId: Long,
        offset: Int,
        limit: Int,
        sortBy: Song.SortBy
    ): Observable<List<Song>> {
        val query = Song.songsByArtistQuery(artistId, sortBy)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor.mapToList(offset, limit) { Song(it) }
            }
    }

    override fun getSongsForGenre(
        genreId: Long,
        offset: Int,
        limit: Int,
        sortBy: Song.SortBy
    ): Observable<List<Song>> {
        val query = Song.songsInGenreQuery(genreId, sortBy)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor.mapToList(offset, limit) { Song(it) }
            }
    }

    override fun getSongById(songId: Long): Observable<Song> {
        val query = Song.songQuery(songId.toString())
        return storeContentResolver.createQuery(query)
            .map { mediaQuery -> mediaQuery.run().mapToOne { Song(it) } }
    }

    override fun getAllSongs(offset: Int, limit: Int, sortBy: Song.SortBy): Observable<List<Song>> {
        val query = Song.allSongsQuery(sortBy)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor.mapToList(offset, limit) { Song(it) }
            }
    }

    override fun getSongsForIds(songIds: List<String>): Observable<List<Song>> {
        val query = Song.songsByIdsQuery(songIds)
        return storeContentResolver.createQuery(query)
            .map { mediaQuery ->
                val cursor = mediaQuery.run()
                cursor.mapToList { Song(it) }
            }
    }
}