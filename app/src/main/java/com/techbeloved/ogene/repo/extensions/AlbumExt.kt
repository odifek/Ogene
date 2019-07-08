package com.techbeloved.ogene.repo.extensions

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.core.net.toUri
import com.techbeloved.ogene.musicbrowser.appendItemId
import com.techbeloved.ogene.repo.models.Album

fun Album.mediaItem(parentId: String): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder().apply {
            setMediaId(parentId.appendItemId(this@mediaItem.id))
            setTitle(this@mediaItem.title)
            setIconUri(this@mediaItem.albumArtUri?.toUri())
            setSubtitle(this@mediaItem.albumArtist)
            setDescription(this@mediaItem.year.toString())
        }.build(),
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )
}

/**
 * Convenience method for converting a list of [Album]s to [MediaBrowserCompat.MediaItem]s
 */
fun List<Album>.mediaItems(parentId: String): List<MediaBrowserCompat.MediaItem> {
    val builder = MediaDescriptionCompat.Builder()
    return this.map { album ->
        MediaBrowserCompat.MediaItem(
            builder.apply {
                setMediaId(parentId.appendItemId(album.id))
                setTitle(album.title)
                setIconUri(album.albumArtUri?.toUri())
                setSubtitle(album.albumArtist)
                setDescription(album.year.toString())
            }.build(),
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        )
    }
}