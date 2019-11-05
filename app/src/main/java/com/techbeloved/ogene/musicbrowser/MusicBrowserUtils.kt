package com.techbeloved.ogene.musicbrowser

import android.content.UriMatcher
import android.net.Uri
import androidx.core.net.toUri
import javax.inject.Inject

class MusicBrowserUtils @Inject constructor() {

    companion object {
        const val SCHEME = "ogene"
        const val AUTHORITY = "com.techbeloved.ogene"

        private const val ALBUMS = 1
        private const val ALBUMS_ID = 1 shl 1
        private const val ALBUMS_SONG_ID = 1 shl 2

        private const val ARTISTS = 1 shl 3
        private const val ARTISTS_ID = 1 shl 4 // Contains both songs and albums by artist
        private const val ARTISTS_SONG_ID = 1 shl 5
        private const val ARTISTS_ALBUMS_ID = 1 shl 6
        private const val ARTISTS_ALBUMS_SONG_ID = 1 shl 7

        private const val GENRES = 1 shl 8
        private const val GENRES_ID = 1 shl 9
        private const val GENRES_SONGS_ID = 1 shl 10

        private const val PLAYLISTS = 1 shl 11
        private const val PLAYLISTS_ID = 1 shl 12
        private const val PLAYLISTS_SONGS_ID = 1 shl 13

        private const val ALL_SONGS = 1 shl 14
        private const val ALL_SONGS_ID = 1 shl 15

        private const val OGENE_ROOT = 1 shl 16

        const val PATH_ALBUMS = "albums"
        const val PATH_ARTISTS = "artists"
        const val PATH_GENRES = "genres"
        const val PATH_PLAYLISTS = "playlists"
        const val PATH_SONGS = "songs"

    }

    private val uriMatcher = UriMatcher(OGENE_ROOT).apply {
        // By Album
        addURI(AUTHORITY, "albums/#/songs/#", ALBUMS_SONG_ID)
        // Should contain both album detail and the songs
        addURI(AUTHORITY, "albums/#", ALBUMS_ID)
        addURI(AUTHORITY, "albums", ALBUMS)

        // By Artist
        addURI(AUTHORITY, "artists/#/albums/#/songs/#", ARTISTS_ALBUMS_SONG_ID)
        addURI(AUTHORITY, "artists/#/albums/#", ARTISTS_ALBUMS_ID)
        addURI(AUTHORITY, "artists/#/songs/#", ARTISTS_SONG_ID)
        addURI(AUTHORITY, "artists/#", ARTISTS_ID)
        addURI(AUTHORITY, "artists", ARTISTS)

        // By Genre
        addURI(AUTHORITY, "genres/#/songs/#", GENRES_SONGS_ID)
        addURI(AUTHORITY, "genres/#", GENRES_ID)
        addURI(AUTHORITY, "genres", GENRES)

        // By Playlist
        addURI(AUTHORITY, "playlists/#/songs/#", PLAYLISTS_SONGS_ID)
        addURI(AUTHORITY, "playlists/#", PLAYLISTS_ID)
        addURI(AUTHORITY, "playlists", PLAYLISTS)

        // All Songs
        addURI(AUTHORITY, "songs/#", ALL_SONGS_ID)
        addURI(AUTHORITY, "songs", ALL_SONGS)
    }

    fun mediaType(uri: Uri): MediaType {
        return when (uriMatcher.match(uri)) {
            ALBUMS_SONG_ID -> {
                val songId = uri.lastPathSegment?.toLong()
                val albumId = uri.pathSegments[1].toLong()
                MediaType.Playable.SongInAlbum(albumId, songId!!)
            }
            ALBUMS_ID -> {
                val albumId = uri.lastPathSegment?.toLong()
                MediaType.Browsable.AlbumDetails(albumId!!)
            }
            ALBUMS -> MediaType.Browsable.Albums

            ARTISTS_ALBUMS_SONG_ID -> {
                val songId = uri.lastPathSegment?.toLong()
                val albumId = uri.pathSegments[3].toLong()
                val artistId = uri.pathSegments[1].toLong()
                MediaType.Playable.SongInAlbumByArtist(artistId, albumId, songId!!)
            }

            ARTISTS_ALBUMS_ID -> {
                val albumId = uri.lastPathSegment?.toLong()
                val artistId = uri.pathSegments[1].toLong()
                MediaType.Browsable.AlbumByArtist(artistId, albumId!!)
            }

            ARTISTS_SONG_ID -> {
                val songId = uri.lastPathSegment?.toLong()
                val artistId = uri.pathSegments[1].toLong()
                MediaType.Playable.SongByArtist(artistId, songId!!)
            }

            ARTISTS_ID -> {
                val artistId = uri.lastPathSegment?.toLong()
                MediaType.Browsable.ArtistDetails(artistId!!)
            }

            ARTISTS -> MediaType.Browsable.Artists

            GENRES_SONGS_ID -> {
                val songId = uri.lastPathSegment?.toLong()
                val genreId = uri.pathSegments[1].toLong()
                MediaType.Playable.SongInGenre(genreId, songId!!)
            }

            GENRES_ID -> {
                val genreId = uri.lastPathSegment?.toLong()
                MediaType.Browsable.GenreDetails(genreId!!)
            }
            GENRES -> MediaType.Browsable.Genres

            PLAYLISTS_SONGS_ID -> {
                val songId = uri.lastPathSegment?.toLong()
                val playlistId = uri.pathSegments[1].toLong()
                MediaType.Playable.SongInPlaylist(playlistId, songId!!)
            }

            PLAYLISTS_ID -> {
                val playlistId = uri.lastPathSegment?.toLong()
                MediaType.Browsable.PlaylistDetails(playlistId!!)
            }

            PLAYLISTS -> MediaType.Browsable.Playlists

            ALL_SONGS_ID -> {
                val songId = uri.lastPathSegment?.toLong()
                MediaType.Playable.SongInAllSongs(songId!!)
            }

            ALL_SONGS -> MediaType.Browsable.AllSongs

            OGENE_ROOT -> MediaType.Browsable.Root // FIXME: Root is not being matched at all. How do you fix it?

            else -> MediaType.Browsable.Root
        }
    }

    fun uriBuilder(): Uri.Builder = Uri.Builder().authority(AUTHORITY).scheme(SCHEME)

    val rootMediaId: String = uriBuilder().build().toString()

    val albumsMediaId: String = uriBuilder().appendPath(PATH_ALBUMS).build().toString()

    val artistsMediaId: String = uriBuilder().appendPath(PATH_ARTISTS).build().toString()

    val genresMediaId: String = uriBuilder().appendPath(PATH_GENRES).build().toString()

    val playlistsMediaId: String = uriBuilder().appendPath(PATH_PLAYLISTS).build().toString()

    val allSongsMediaId: String = uriBuilder().appendPath(PATH_SONGS).build().toString()

}

sealed class MediaType {
    sealed class Playable : MediaType() {

        // By Album
        data class SongInAlbum(val albumId: Long, val songId: Long) : Playable()

        data class SongByArtist(val artistId: Long, val songId: Long) : Playable()
        data class SongInAlbumByArtist(val artistId: Long, val albumId: Long, val songId: Long) :
            Playable()

        data class SongInGenre(val genreId: Long, val songId: Long) : Playable()
        data class SongInPlaylist(val playlistId: Long, val songId: Long) : Playable()

        data class SongInAllSongs(val songId: Long) : Playable()

    }

    sealed class Browsable : MediaType() {
        object Root : Browsable()
        object Albums : Browsable()
        data class AlbumDetails(val id: Long) : Browsable()

        object Artists : Browsable()
        /**
         * @param id artist id
         */
        data class ArtistDetails(val id: Long) : Browsable()

        data class AlbumByArtist(val artistId: Long, val albumId: Long) : Browsable()

        object Genres : Browsable()
        data class GenreDetails(val id: Long) : Browsable()

        object Playlists : Browsable()
        data class PlaylistDetails(val id: Long) : Browsable()

        object AllSongs : Browsable()
    }

    object Unknown: MediaType()
}

/**
 * Id of the song which is the last segment of the uri
 */
fun String.songItemId(): Long? = this.toUri().lastPathSegment?.toLong()

/**
 * Get the parent id of the song
 */
fun String.songParentMediaId(): String = this.substringBeforeLast("/songs")