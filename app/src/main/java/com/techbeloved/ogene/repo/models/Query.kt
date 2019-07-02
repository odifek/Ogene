package com.techbeloved.ogene.repo.models

import android.net.Uri

/**
 * Use to build query for our MediaStore
 */
class Query(
    val uri: Uri?,
    val projection: Array<String>?,
    val selection: String?,
    val args: Array<String>?,
    val sort: String?
) {

    data class Builder(
        var uri: Uri? = null,
        var projection: List<String>? = null,
        var selection: String? = null,
        var args: List<String>? = null,
        var sort: String? = null
    ) {
        fun uri(uri: Uri) = apply { this.uri = uri }
        fun projection(projection: List<String>?) = apply { this.projection = projection }
        fun selection(selection: String) = apply { this.selection = selection }
        fun args(args: List<String>?) = apply { this.args = args }
        fun sort(sort: String?) = apply { this.sort = sort }
        fun build() = Query(
            this.uri,
            this.projection?.toTypedArray(),
            this.selection,
            this.args?.toTypedArray(),
            this.sort
        )
    }


}