package com.techbeloved.ogene.musicbrowser

const val AUTHORITY = "com.techbeloved.ogene"
const val SCHEME = "content"

const val CATEGORY_ALBUMS = "albums"
const val CATEGORY_ALL_SONGS = "all_songs"
const val CATEGORY_ARTISTS = "artists"
const val CATEGORY_PLAYLISTS = "playlist"
const val CATEGORY_GENRES = "genres"

/**
 * The root of all roots
 */
const val CATEGORY_ROOT = """$SCHEME://$AUTHORITY"""


private const val TOP_LEVEL_CATEGORIES = "$CATEGORY_ALBUMS|$CATEGORY_ALL_SONGS|$CATEGORY_ARTISTS|$CATEGORY_GENRES|$CATEGORY_PLAYLISTS"

/**
 * Matches a valid
 * root:-                content://com.techbeloved.ogene/
 *
 */

const val ROOT_REGEX = """($SCHEME)://$AUTHORITY/?${'$'}"""

/**
 * Matches a url string of the form
 * category:            content://com.techbeloved.ogene/{category}
 * category details:    content://com.techbeloved.ogene/{category}/{categoryId}
 * category song item:  content://com.techbeloved.ogene/{category}/{categoryId}/songs/{songId}
 **/

const val CATEGORY_TOP_LEVEL_REGEX = """($SCHEME)://$AUTHORITY/($TOP_LEVEL_CATEGORIES|$CATEGORY_ALL_SONGS)/?${'$'}"""

const val CATEGORY_REGEX = """($SCHEME)://$AUTHORITY/($TOP_LEVEL_CATEGORIES)/(\d+)/?${'$'}"""

const val SONG_ITEM_REGEX = """($SCHEME)://$AUTHORITY/($TOP_LEVEL_CATEGORIES)/(\d+)/songs/(\d+)/?${'$'}"""

const val EXTRACT_CATEGORY_URI_FROM_SONG_URI_REGEX =
    """($SCHEME://$AUTHORITY/\w+/\d+)/songs/(\d+)/?${'$'}"""

/**
 * Matches sub categories
 * content://com.techbeloved.ogene/{category}/{categoryId}/{subcategory}/{subcategoryId}
 * content://com.techbeloved.ogene/{category}/{categoryId}/{subcategory}/{subcategoryId}/songs/{songId}
 */

const val SUB_CATEGORY_REGEX = """($SCHEME)://$AUTHORITY/(\w+)/(\d+)/(\w+)/(\d+)/?${'$'}"""
const val SUB_CATEGORY_LISTING_REGEX = """($SCHEME)://$AUTHORITY/($TOP_LEVEL_CATEGORIES)/(\d+)/($TOP_LEVEL_CATEGORIES)/?${'$'}"""
const val SUB_CATEGORY_SONG_ITEM_REGEX =
    """($SCHEME)://$AUTHORITY/(\w+)/(\d+)/(\w+)/(\d+)/songs/(\d+)/?${'$'}"""
const val EXTRACT_SUB_CATEGORY_URI_FROM_SONG_ITEM_REGEX =
    """($SCHEME://$AUTHORITY/\w+/\d+/\w+/\d+)/songs/(\d+)/?${'$'}"""
const val EXTRACT_CATEGORY_URI_FROM_SUB_CATEGORY_REGEX =  """($SCHEME://$AUTHORITY/\w+/\d+)/\w+/\d+/?${'$'}"""

/**
 * Retrieve category name from music uri. Returns null if uri is not valid category uri
 */
fun String.category(): String? {
    val topLevelCategoryRegex = CATEGORY_TOP_LEVEL_REGEX.toRegex()
    val categoryRegex = CATEGORY_REGEX.toRegex()
    val subCategoryRegex = SUB_CATEGORY_REGEX.toRegex()
    val subCategoryItemRegex = SUB_CATEGORY_SONG_ITEM_REGEX.toRegex()
    val subCategoryListingRegex = SUB_CATEGORY_LISTING_REGEX.toRegex()
    val itemRegex = SONG_ITEM_REGEX.toRegex()

    val rootRegex = ROOT_REGEX.toRegex()

    return when {
        topLevelCategoryRegex matches this -> {
            val matchResult = topLevelCategoryRegex.find(this)
            matchResult?.groupValues?.get(2)
        }
        categoryRegex matches this -> {
            val matchResult = categoryRegex.find(this)
            matchResult?.groupValues?.get(2)
        }
        itemRegex matches this -> {
            val matchResult = itemRegex.find(this)
            matchResult?.groupValues?.get(2)
        }
        subCategoryRegex matches this -> {
            val matchResult = subCategoryRegex.find(this)
            matchResult?.groupValues?.get(2)
        }
        subCategoryListingRegex matches this -> {
            val matchResult = subCategoryListingRegex.find(this)
            matchResult?.groupValues?.get(2)
        }

        subCategoryItemRegex matches this -> {
            val matchResult = subCategoryItemRegex.find(this)
            matchResult?.groupValues?.get(2)
        }

        rootRegex matches this -> {
            val matchResult = rootRegex.find(this)
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
    val subCategoryRegex = SUB_CATEGORY_REGEX.toRegex()
    val subCategoryItemRegex = SUB_CATEGORY_SONG_ITEM_REGEX.toRegex()
    val subCategoryListingRegex = SUB_CATEGORY_LISTING_REGEX.toRegex()

    return when {
        categoryRegex matches this -> {
            val matchResult = categoryRegex.find(this)
            matchResult?.groupValues?.get(3)?.toLongOrNull()
        }
        itemRegex matches this -> {
            val matchResult = itemRegex.find(this)
            matchResult?.groupValues?.get(3)?.toLongOrNull()
        }
        subCategoryRegex matches this -> {
            val matchResult = subCategoryRegex.find(this)
            matchResult?.groupValues?.get(3)?.toLongOrNull()
        }

        subCategoryItemRegex matches this -> {
            val matchResult = subCategoryItemRegex.find(this)
            matchResult?.groupValues?.get(3)?.toLongOrNull()
        }
        subCategoryListingRegex matches this -> {
            val matchResult = subCategoryListingRegex.find(this)
            matchResult?.groupValues?.get(3)?.toLongOrNull()
        }
        else -> null
    }
}

/**
 * Extracts the sub category from the given uri
 */
fun String.subCategory(): String? {
    val subCategoryRegex = SUB_CATEGORY_REGEX.toRegex()
    val subCategoryItemRegex = SUB_CATEGORY_SONG_ITEM_REGEX.toRegex()
    val subCategoryListingRegex = SUB_CATEGORY_LISTING_REGEX.toRegex()

    return when {
        subCategoryRegex matches this -> {
            val matchResult = subCategoryRegex.find(this)
            matchResult?.groupValues?.get(4)
        }

        subCategoryItemRegex matches this -> {
            val matchResult = subCategoryItemRegex.find(this)
            matchResult?.groupValues?.get(4)
        }
        subCategoryListingRegex matches this -> {
            val matchResult = subCategoryListingRegex.find(this)
            matchResult?.groupValues?.get(4)
        }
        else -> null
    }
}

/**
 * Extracts the sub category id from the given uri
 */
fun String.subCategoryId(): Long? {
    val subCategoryRegex = SUB_CATEGORY_REGEX.toRegex()
    val subCategoryItemRegex = SUB_CATEGORY_SONG_ITEM_REGEX.toRegex()

    return when {
        subCategoryRegex matches this -> {
            val matchResult = subCategoryRegex.find(this)
            matchResult?.groupValues?.get(5)?.toLongOrNull()
        }

        subCategoryItemRegex matches this -> {
            val matchResult = subCategoryItemRegex.find(this)
            matchResult?.groupValues?.get(5)?.toLongOrNull()
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
    val itemSubRegex = SUB_CATEGORY_SONG_ITEM_REGEX.toRegex()

    return when {
        itemRegex matches this -> {
            val matchResult = itemRegex.find(this)
            matchResult?.groupValues?.get(4)?.toLongOrNull()
        }
        itemSubRegex matches this -> {
            val matchResult = itemSubRegex.find(this)
            matchResult?.groupValues?.get(6)?.toLongOrNull()
        }
        else -> null
    }
}

/**
 * Checks that the uri is of the form -
 * content://com.techbeloved.ogene/{category}/{categoryId}
 */
fun String.isValidCategoryUri(): Boolean {
    val categoryRegex = CATEGORY_REGEX.toRegex()
    return categoryRegex matches this
}

fun String.isValidTopLevelCategory() = CATEGORY_TOP_LEVEL_REGEX.toRegex() matches this

fun String.isValidSubCategoryListing() = SUB_CATEGORY_LISTING_REGEX.toRegex() matches this

/**
 * Checks that the uri is of the form - content://com.techbeloved.ogene/{category}/{categoryId}/{subcategory}/{subcategoryId}
 */
fun String.isValidSubCategoryUri(): Boolean {
    return SUB_CATEGORY_REGEX.toRegex() matches this
}

fun String.isValidSongUri(): Boolean {
    val categoryRegex = SONG_ITEM_REGEX.toRegex()
    return (categoryRegex matches this) || (SUB_CATEGORY_SONG_ITEM_REGEX.toRegex() matches this)
}

fun String.isValidRootUri(): Boolean = ROOT_REGEX.toRegex() matches this


/**
 * Uses string concatenation to build a simple category uri
 */
fun buildCategoryUri(category: String, categoryId: Int): String {
    return "$SCHEME://$AUTHORITY/$category/$categoryId"
}

fun buildSubCategoryUri(parentUri: String, subCategory: String, subCategoryId: Long): String {
    return "$parentUri/$subCategory/$subCategoryId"
}

fun String.appendItemId(itemId: Long): String? {
    if (this.isValidTopLevelCategory() || this.isValidSubCategoryListing()) {
        return "$this/$itemId"
    }
    return null
}

/**
 * Given parent browsing id, attach the required song id
 */
fun String.appendSongId(songId: Long): String? {
    if (this.isValidCategoryUri() || this.isValidSubCategoryUri()) {
        return "$this/songs/$songId"
    }
    return null
}

/**
 * Use to retrieve the parent Category browsing uri given a browsing song uri
 */
fun String.parentCategoryUri(): String? {
    val extractCategoryRegex = EXTRACT_CATEGORY_URI_FROM_SONG_URI_REGEX.toRegex()
    val extractSubCategoryRegex = EXTRACT_SUB_CATEGORY_URI_FROM_SONG_ITEM_REGEX.toRegex()
    val extractCategoryFromSubCategoryRegex = EXTRACT_CATEGORY_URI_FROM_SUB_CATEGORY_REGEX.toRegex()
    val matchResult = when {
        extractCategoryRegex matches this -> extractCategoryRegex.find(this)
        extractCategoryFromSubCategoryRegex matches this -> extractCategoryFromSubCategoryRegex.find(this)
        else -> extractSubCategoryRegex.find(this)
    }
    return matchResult?.groupValues?.get(1)
}