package com.techbeloved.ogene.musicbrowser

const val AUTHORITY = "com.techbeloved.ogene"
const val SCHEME = "content"

const val CATEGORY_ALBUMS = "albums"
const val CATEGORY_ALL_SONGS = "all_songs"
const val CATEGORY_ARTISTS = "artists"
const val CATEGORY_PLAYLISTS = "playlist"
const val CATEGORY_GENRES = "genres"

/**
 * Matches a url string of the form content://com.techbeloved.ogene/{category}/{categoryId}
 *
 **/
const val CATEGORY_REGEX = """($SCHEME)://$AUTHORITY/(\w+)/(\d+)/?$"""

const val SONG_ITEM_REGEX = """($SCHEME)://$AUTHORITY/(\w+)/(\d+)/songs/(\d+)/?$"""

const val EXTRACT_CATEGORY_URI_FROM_SONG_URI_REGEX = """($SCHEME://$AUTHORITY/\w+/\d+)/songs/(\d+)/?${'$'}"""

/**
 * Retrieve category name from music uri. Returns null if uri is not valid category uri
 */
fun String.category(): String? {
    val categoryRegex = CATEGORY_REGEX.toRegex()
    val itemRegex = SONG_ITEM_REGEX.toRegex()

    return when {
        categoryRegex matches this -> {
            val matchResult = categoryRegex.find(this)
            matchResult?.groupValues?.get(2)
        }
        itemRegex matches this -> {
            val matchResult = itemRegex.find(this)
            matchResult?.groupValues?.get(2)
        }
        else -> null
    }
}

/**
 * Extracts category id from a music uri.
 * @return category id or null if uri is not valid
 */
fun String.categoryId(): Long? {
    val categoryRegex = CATEGORY_REGEX.toRegex()
    val itemRegex = SONG_ITEM_REGEX.toRegex()

    return when {
        categoryRegex matches this -> {
            val matchResult = categoryRegex.find(this)
            matchResult?.groupValues?.get(3)?.toLongOrNull()
        }
        itemRegex matches this -> {
            val matchResult = itemRegex.find(this)
            matchResult?.groupValues?.get(3)?.toLongOrNull()
        }
        else -> null
    }
}

/**
 * Extracts songId from a song uri-like id.
 * @return song id or null if uri is not a valid uri pointing to a song item
 */
fun String.songId(): Long? {
    val itemRegex = SONG_ITEM_REGEX.toRegex()

    return if (itemRegex matches this) {
        val matchResult = itemRegex.find(this)
        matchResult?.groupValues?.get(4)?.toLongOrNull()
    } else null
}

fun String.isValidCategoryUri(): Boolean {
    val categoryRegex = CATEGORY_REGEX.toRegex()
    return categoryRegex matches this
}

fun String.isValidSongUri(): Boolean {
    val categoryRegex = SONG_ITEM_REGEX.toRegex()
    return categoryRegex matches this
}

/**
 * Uses string concatenation to build a simple category uri
 */
fun buildCategoryUri(category: String, categoryId: Int): String {
    return "$SCHEME://$AUTHORITY/$category/$categoryId"
}

/**
 * Given parent browsing id, attach the required song id
 */
fun String.appendSongId(songId: Long): String? {
    if (this.isValidCategoryUri()) {
        return "$this/songs/$songId"
    }
    return null
}

/**
 * Use to retrieve the parent Category browsing uri given a browsing song uri
 */
fun String.parentCategoryUri(): String? {
    val itemUriRegex = EXTRACT_CATEGORY_URI_FROM_SONG_URI_REGEX.toRegex()
    return if (itemUriRegex matches this) {
        val matchResult = itemUriRegex.find(this)
        matchResult?.groupValues?.get(1)
    } else null
}