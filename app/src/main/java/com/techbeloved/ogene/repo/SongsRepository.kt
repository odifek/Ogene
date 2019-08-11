package com.techbeloved.ogene.repo

import com.techbeloved.ogene.repo.models.Song
import io.reactivex.Observable

interface SongsRepository {

    fun getSongsForAlbum(albumId: Long, offset: Int = 0, limit: Int = -1, sortBy: Song.SortBy = Song.SortBy.TRACK): Observable<List<Song>>

    fun getSongsForArtist(artistId: Long, offset: Int = 0, limit: Int = -1, sortBy: Song.SortBy = Song.SortBy.ALBUM): Observable<List<Song>>

    fun getSongsForGenre(genreId: Long, offset: Int = 0, limit: Int = -1, sortBy: Song.SortBy = Song.SortBy.ALBUM): Observable<List<Song>>

    fun getSongById(songId: Long): Observable<Song>

    fun getAllSongs(offset: Int = 0, limit: Int = -1, sortBy: Song.SortBy = Song.SortBy.ALBUM): Observable<List<Song>>

    fun getSongsForIds(songIds: List<String>): Observable<List<Song>>

}