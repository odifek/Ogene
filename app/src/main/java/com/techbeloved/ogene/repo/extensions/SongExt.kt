package com.techbeloved.ogene.repo.extensions

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.techbeloved.ogene.repo.models.Song

/**
 * Construct a [MediaMetadataCompat] from [Song] properties
 */
val Song.metadata: MediaMetadataCompat
    get() {
        return MediaMetadataCompat.Builder().apply {
            title = this@metadata.title
            mediaId = this@metadata.mediaId!!
            mediaUri = this@metadata.contentUri?.toString()
            album = this@metadata.album
            artist = this@metadata.artist
            albumArtUri = this@metadata.albumArtUri
            trackNumber = this@metadata.track
            trackCount = 1
            dateAdded = this@metadata.dateAdded
        }.build()

    }

/**
 * Construct a browsing mediaItem for [Song]. This is useful for UI so we can determine the category and other things
 */
fun Song.mediaItem(parentId: String): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder().apply {
            setMediaId("$parentId/songs/${this@mediaItem.id}")
            setMediaUri(this@mediaItem.contentUri)
            setTitle(this@mediaItem.title)
            setSubtitle(this@mediaItem.album)
            setDescription(this@mediaItem.artist)
            setExtras(Bundle().apply {
                putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, this@mediaItem.track)
                putInt(MediaMetadataCompat.METADATA_KEY_YEAR, this@mediaItem.year)
                putLong(MediaMetadataCompat.METADATA_KEY_DATE, this@mediaItem.dateAdded)
            })
        }.build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )
}

fun List<Song>.mediaItems(parentId: String): List<MediaBrowserCompat.MediaItem> {
    val builder = MediaDescriptionCompat.Builder()
    return this.map { song ->
        MediaBrowserCompat.MediaItem(
            builder.apply {
                setMediaId("$parentId/songs/${song.id}")
                setMediaUri(song.contentUri)
                setTitle(song.title)
                setSubtitle(song.album)
                setDescription(song.artist)
                setExtras(Bundle().apply {
                    putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, song.track)
                    putInt(MediaMetadataCompat.METADATA_KEY_YEAR, song.year)
                    putLong(MediaMetadataCompat.METADATA_KEY_DATE, song.dateAdded)
                })
            }.build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }
}

