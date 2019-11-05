package com.techbeloved.ogene.repo.extensions

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.techbeloved.ogene.repo.models.Genre

fun Genre.mediaItem(parentId: String): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder().apply {
            setMediaId("$parentId/${this@mediaItem.id}")
            setTitle(this@mediaItem.name)
        }.build(),
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )
}

fun List<Genre>.mediaItems(parentId: String): List<MediaBrowserCompat.MediaItem> {
    val builder = MediaDescriptionCompat.Builder()
    return this.map { genre ->
        MediaBrowserCompat.MediaItem(
            builder.apply {
                setMediaId("$parentId/${genre.id}")
                setTitle(genre.name)
            }.build(),
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        )
    }
}