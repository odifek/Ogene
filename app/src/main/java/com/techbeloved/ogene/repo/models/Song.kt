package com.techbeloved.ogene.repo.models

import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat

class Song() {

    var id: Long = 0
    var duration: Long = 0
    var title: String? = null
    var album: String? = null
    var albumId: Long = 0
    var year: Int = 0
    var artist: String? = null
    var artistId: Long = 0
    var contentUri: Uri? = null
    var dateAdded: Long = 0
    var bookmark: Long = 0
    var track: Long = 0

    var mediaId: String? = null

    var albumArtUri: String? = null
    var genres: List<String>? = null
    var mimeType: String? = null
    var data1: String? = null
    var data2: String? = null

    val songMeta: MediaMetadataCompat
        @SuppressLint("WrongConstant")
        get() {
            val builder = MediaMetadataCompat.Builder()

            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, contentUri.toString())
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, track)
            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
            builder.putLong("MEDIA_ITEM_FLAG", MediaBrowserCompat.MediaItem.FLAG_PLAYABLE.toLong())
            return builder.build()
        }

    constructor(cursor: Cursor, search: String) : this() {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST))
        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM))
        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE))
        data1 = cursor.getString(cursor.getColumnIndexOrThrow("data1"))
        data2 = cursor.getString(cursor.getColumnIndexOrThrow("data2"))
    }

    constructor(cursor: Cursor) : this() {
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE))
        album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM))
        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST))
        id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)).toLong()
        duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION))
        albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID))
        year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.YEAR))
        artistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID))
        dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_ADDED))
        bookmark = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.BOOKMARK))
        track = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TRACK))
        contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
        // Eg 1234_even_i
        mediaId = id.toString()
        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.MIME_TYPE))
    }

    companion object {

        private val projection: Array<String> = arrayOf(
            "MediaStore.Audio.AudioColumns.TITLE",
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.YEAR,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.BOOKMARK,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.Media.DATA
        )
        private const val selection = ("("
                + "(" + MediaStore.Audio.Media.IS_MUSIC + " !=0 )"
                + "AND (" + MediaStore.Audio.Media.IS_ALARM + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_NOTIFICATION + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_PODCAST + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_RINGTONE + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.MIME_TYPE + "!='audio/midi' )"
                + ")")
        private const val sortOrder = MediaStore.Audio.Media.ARTIST + " ASC"
        val allSongsQuery: Query
            get() = Query.Builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(selection)
                .args(null)
                .sort(sortOrder)
                .build()

        // Query for getting a single item
        fun songQuery(id: String): Query {
            val selection = MediaStore.Audio.Media.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.Media._ID + "=?"
            return Query.Builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(selection)
                .args(listOf(id))
                .sort(MediaStore.Audio.Media.TITLE)
                .build()
        }

        fun songsInAlbumQuery(albumId: Long): Query {
            val selection = "(${MediaStore.Audio.Media.ALBUM_ID}=?)"
            return Query.Builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(selection)
                .args(listOf(albumId.toString()))
                .sort(getSortOrder(SortBy.TRACK))
                .build()
        }

        fun songsByArtistQuery(artistId: Long): Query {
            val selection = "(${MediaStore.Audio.Media.ARTIST_ID}=?)"
            return Query.Builder()
                .uri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .projection(null)
                .selection(selection)
                .args(listOf(artistId.toString()))
                .sort(getSortOrder(SortBy.ALBUM))
                .build()
        }

        fun songsInGenreQuery(genreId: Long, sortBy: SortBy = SortBy.ALBUM): Query {
            val uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
            val sortOrder = getSortOrder(sortBy)
            return Query.Builder()
                .uri(uri)
                .projection(null)
                .selection(selection)
                .args(null)
                .sort(sortOrder)
                .build()
        }

        private fun getSortOrder(sortBy: SortBy = SortBy.TITLE): String {
            return when (sortBy) {
                SortBy.TITLE -> MediaStore.Audio.Media.DEFAULT_SORT_ORDER
                SortBy.ALBUM -> MediaStore.Audio.Media.ALBUM_KEY
                SortBy.YEAR -> MediaStore.Audio.Media.YEAR
                SortBy.ARTIST -> MediaStore.Audio.Media.ARTIST_KEY
                SortBy.DATE_ADDED -> MediaStore.Audio.Media.DATE_ADDED
                SortBy.TRACK -> MediaStore.Audio.Media.TRACK + " ASC"

            }
        }
    }

    enum class SortBy {
        TITLE, ALBUM, YEAR, ARTIST, DATE_ADDED, TRACK
    }
}