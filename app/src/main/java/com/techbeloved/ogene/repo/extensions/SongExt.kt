package com.techbeloved.ogene.repo.extensions

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.techbeloved.ogene.musicbrowser.appendSongId
import com.techbeloved.ogene.repo.models.Song

/**
 * Construct a [MediaMetadataCompat] from [Song] properties
 */
val Song.metadata: MediaMetadataCompat get() {
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
            setMediaId(parentId.appendSongId(this@mediaItem.id))
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