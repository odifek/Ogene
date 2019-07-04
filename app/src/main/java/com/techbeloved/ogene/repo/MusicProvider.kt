package com.techbeloved.ogene.repo

import android.support.v4.media.MediaBrowserCompat
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
                }
            }
        }

    }
}