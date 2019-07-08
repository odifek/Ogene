package com.techbeloved.ogene.repo

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.techbeloved.ogene.musicbrowser.*
import com.techbeloved.ogene.repo.extensions.mediaItems
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicProvider @Inject constructor(
    private val songsRepository: SongsRepository,
    private val albumsRepository: AlbumsRepository,
    private val artistsRepository: ArtistsRepository,
    private val genresRepository: GenresRepository
) {
    fun getMediaItemsForMediaId(
        parentId: String,
        offset: Int = 0,
        limit: Int = -1
    ): Single<List<MediaBrowserCompat.MediaItem>> {
        if (!parentId.isValidCategoryUri() && !parentId.isValidSubCategoryUri()
            && !parentId.isValidTopLevelCategory() && !parentId.isValidRootUri()
            && !parentId.isValidSongUri() && !parentId.isValidSubCategoryListing()
        ) {
            return Single.error(Throwable("Invalid id supplied: $parentId"))
        }

        return when {

            parentId.isValidRootUri() -> {
                return Single.just(rootMediaItems())
            }

            parentId.isValidTopLevelCategory() -> {
                when (parentId.category()) {
                    CATEGORY_ALBUMS -> albumsRepository.getAlbums(offset, limit).firstOrError()
                        .map { it.mediaItems(parentId) }
                    CATEGORY_ARTISTS -> artistsRepository.getAllArtists(offset, limit).firstOrError()
                        .map { it.mediaItems(parentId) }
                    CATEGORY_GENRES -> genresRepository.getAllGenres(offset, limit).firstOrError()
                        .map { it.mediaItems(parentId) }
                    // TODO: add other top level categories like playlist, etc
                    CATEGORY_ALL_SONGS -> songsRepository.getAllSongs(offset, limit).firstOrError()
                        .map { it.mediaItems("$parentId/0") }
                    else -> Single.error(Throwable("Category unrecognized or not yet implemented! ${parentId.category()}"))
                }
            }
            parentId.isValidCategoryUri() -> {
                val category = parentId.category()
                val categoryId = parentId.categoryId() ?: 0
                when (category) {
                    CATEGORY_ALBUMS -> songsRepository.getSongsForAlbum(categoryId, offset, limit).firstOrError()
                        .map { it.mediaItems(parentId) }
                    CATEGORY_ALL_SONGS -> songsRepository.getAllSongs(offset, limit).firstOrError()
                        .map { it.mediaItems(parentId) }
                    CATEGORY_ARTISTS -> songsRepository.getSongsForArtist(categoryId, offset, limit)
                        .map { it.mediaItems(parentId) }
                        .flatMap { songItems ->
                            albumsRepository.getAlbumsForArtist(categoryId)
                                .map { albums -> songItems.plus(albums.mediaItems("$parentId/$CATEGORY_ALBUMS")) }
                        }.firstOrError()
                    CATEGORY_GENRES -> songsRepository.getSongsForGenre(categoryId, offset, limit).firstOrError()
                        .map { it.mediaItems(parentId) }
                    else -> Single.error(Throwable("invalid or not implemented category: $category"))
                }
            }
            parentId.isValidSubCategoryUri() -> {
                val category = parentId.category()
                val categoryId = parentId.categoryId()
                val subCategory = parentId.subCategory()
                val subCategoryId = parentId.subCategoryId() ?: 0
                when (category) {
                    CATEGORY_ARTISTS -> {
                        when (subCategory) {
                            CATEGORY_ALBUMS -> songsRepository.getSongsForAlbum(subCategoryId, offset, limit)
                                .firstOrError()
                                .map { it.mediaItems(parentId) }
                            else -> Single.error(Throwable("Category invalid or not implemented"))
                        }
                    }
                    else -> Single.error(Throwable("Category invalid or not implemented! $category"))

                }
            }
            else -> return Single.error(Throwable("Category invalid or not implemented! $parentId"))
        }

    }

    private fun rootMediaItems(): List<MediaBrowserCompat.MediaItem> {
        val builder = MediaDescriptionCompat.Builder()
        return listOf(
            MediaBrowserCompat.MediaItem(
                builder.setMediaId("$CATEGORY_ROOT/$CATEGORY_ALL_SONGS")
                    .setTitle("All Songs")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                builder.setMediaId("$CATEGORY_ROOT/$CATEGORY_ALBUMS")
                    .setTitle("Albums")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                builder.setMediaId("$CATEGORY_ROOT/$CATEGORY_ARTISTS")
                    .setTitle("Artists")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                builder.setMediaId("$CATEGORY_ROOT/$CATEGORY_PLAYLISTS")
                    .setTitle("Playlists")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                builder.setMediaId("$CATEGORY_ROOT/$CATEGORY_GENRES")
                    .setTitle("Genres")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            )
        )
    }
}