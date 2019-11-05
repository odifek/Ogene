package com.techbeloved.ogene.repo.extensions

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.techbeloved.ogene.repo.models.Artist

fun Artist.mediaItem(parentId: String): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder().apply {
            setMediaId("$parentId/${this@mediaItem.id}")
            setTitle(this@mediaItem.title)
            setSubtitle("Appeared in ${this@mediaItem.numberOfAlbums} albums")
        }.build(),
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )
}

fun List<Artist>.mediaItems(parentId: String): List<MediaBrowserCompat.MediaItem> {
    val builder = MediaDescriptionCompat.Builder()
    return this.map { artist ->
        MediaBrowserCompat.MediaItem(
            builder.apply {
                setMediaId("$parentId/${artist.id}")
                setTitle(artist.title)
                setSubtitle("Appeared in ${artist.numberOfAlbums} albums")
            }.build(),
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        )
    }
}