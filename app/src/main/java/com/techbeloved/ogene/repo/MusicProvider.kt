package com.techbeloved.ogene.repo

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.techbeloved.ogene.musicbrowser.*
import com.techbeloved.ogene.repo.extensions.mediaItem
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
    fun getMediaItemsForMediaId(parentId: String, offset: Int, limit: Int): Single<List<MediaBrowserCompat.MediaItem>> {
        if (!parentId.isValidCategoryUri() || parentId.isValidSubCategoryUri() || parentId.isValidSongUri()) {
            return Single.error(Throwable("Invalid id supplied: $parentId"))
        }

        return when {
            parentId.isValidRootUri() -> {
                return Single.just(rootMediaItems())
            }
            parentId.isValidCategoryUri() -> {
                val category = parentId.category()
                val categoryId = parentId.categoryId() ?: 0
                when (category) {
                    CATEGORY_ALBUMS -> songsRepository.getSongsForAlbum(categoryId, offset, limit).firstOrError()
                        .map { songs -> songs.map { song -> song.mediaItem(parentId) } }
                    CATEGORY_ALL_SONGS -> songsRepository.getAllSongs(offset, limit).firstOrError()
                        .map { songs -> songs.map { song -> song.mediaItem(parentId) } }
                    CATEGORY_ARTISTS -> songsRepository.getSongsForArtist(categoryId, offset, limit).firstOrError()
                        .map { songs -> songs.map { song -> song.mediaItem(parentId) } }
                    CATEGORY_GENRES -> songsRepository.getSongsForGenre(categoryId, offset, limit).firstOrError()
                        .map { songs -> songs.map { song -> song.mediaItem(parentId) } }
                    else -> Single.error(Throwable("invalid or not implemented category: $category"))
                }
            }
            else -> return Single.error(Throwable("Invalid category"))
        }

    }

    private fun rootMediaItems(): List<MediaBrowserCompat.MediaItem> {
        val builder = MediaDescriptionCompat.Builder()
        return listOf(
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