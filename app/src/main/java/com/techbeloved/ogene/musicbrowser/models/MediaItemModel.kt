package com.techbeloved.ogene.musicbrowser.models

interface MediaItemModel {

    /**
     * Unique id of the media item, which could be, for a local song, the MediaStore id
     */
    val id: Long?

    val title: String

    val subtitle: String?

    val description: String?

    /**
     * Could represent the browsing media id which looks like a uri
     * Example: content://com.techbeloved.ogene/albums/{albumId}/songs/{songId}
     */
    val mediaId: String?
}

data class SimpleMediaItem(
    override val id: Long?,
    override val title: String,
    override val subtitle: String? = null,
    override val description: String? = null,
    override val mediaId: String?
) : MediaItemModel
