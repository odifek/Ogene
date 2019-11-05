package com.techbeloved.ogene.repo

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.core.net.toUri
import com.techbeloved.ogene.musicbrowser.MediaType
import com.techbeloved.ogene.musicbrowser.MusicBrowserUtils
import com.techbeloved.ogene.repo.extensions.mediaItems
import com.techbeloved.ogene.repo.models.NowPlayingItem
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicProvider @Inject constructor(
    private val songsRepository: SongsRepository,
    private val albumsRepository: AlbumsRepository,
    private val artistsRepository: ArtistsRepository,
    private val genresRepository: GenresRepository,
    private val sharedPreferencesRepo: SharedPreferencesRepo,
    private val musicBrowserUtils: MusicBrowserUtils
) {
    fun getMediaItemsForMediaId(
        parentId: String,
        offset: Int = 0,
        limit: Int = -1
    ): Single<List<MediaBrowserCompat.MediaItem>> {

        return when (val mediaType = musicBrowserUtils.mediaType(parentId.toUri())) {

            MediaType.Unknown -> Single.error(IllegalStateException("Unrecognized media id: $parentId"))
            is MediaType.Browsable.Root -> Single.just(rootMediaItems())

            is MediaType.Playable.SongInAlbum -> TODO()
            is MediaType.Playable.SongByArtist -> TODO()
            is MediaType.Playable.SongInAlbumByArtist -> TODO()
            is MediaType.Playable.SongInGenre -> TODO()
            is MediaType.Playable.SongInPlaylist -> TODO()
            is MediaType.Playable.SongInAllSongs -> TODO()
            MediaType.Browsable.Albums -> albumsRepository.getAlbums(offset, limit).firstOrError()
                .map { it.mediaItems(parentId) }
            is MediaType.Browsable.AlbumDetails ->
                songsRepository.getSongsForAlbum(mediaType.id, offset, limit).firstOrError()
                    .map { it.mediaItems(parentId) }
            is MediaType.Browsable.Artists ->
                artistsRepository.getAllArtists(offset, limit).firstOrError()
                    .map { it.mediaItems(parentId) }
            is MediaType.Browsable.ArtistDetails ->
                songsRepository.getSongsForArtist(mediaType.id, offset, limit)
                    .map { it.mediaItems(parentId) }
                    .flatMap { songItems ->
                        albumsRepository.getAlbumsForArtist(mediaType.id)
                            .map { albums -> songItems.plus(albums.mediaItems("$parentId/albums")) } // TODO: This is probably not correct
                    }.firstOrError()
            is MediaType.Browsable.AlbumByArtist ->
                songsRepository.getSongsForAlbum(mediaType.albumId, offset, limit)
                    .firstOrError()
                    .map { it.mediaItems(parentId) }
            is MediaType.Browsable.Genres ->
                genresRepository.getAllGenres(offset, limit).firstOrError()
                    .map { it.mediaItems(parentId) }
            is MediaType.Browsable.GenreDetails ->
                songsRepository.getSongsForGenre(mediaType.id, offset, limit).firstOrError()
                    .map { it.mediaItems(parentId) }
            is MediaType.Browsable.Playlists -> TODO()
            is MediaType.Browsable.PlaylistDetails -> TODO()
            is MediaType.Browsable.AllSongs ->
                songsRepository.getAllSongs(offset, limit).firstOrError()
                    .map { it.mediaItems(parentId) }
        }

    }

    fun saveQueueItems(songIds: List<String>) {
        sharedPreferencesRepo.saveQueue(songIds)
    }

    private fun rootMediaItems(): List<MediaBrowserCompat.MediaItem> {
        val builder = MediaDescriptionCompat.Builder()
        return listOf(
            MediaBrowserCompat.MediaItem(
                builder.setMediaId(musicBrowserUtils.allSongsMediaId)
                    .setTitle("All Songs")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                builder.setMediaId(musicBrowserUtils.albumsMediaId)
                    .setTitle("Albums")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                builder.setMediaId(musicBrowserUtils.artistsMediaId)
                    .setTitle("Artists")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                builder.setMediaId(musicBrowserUtils.playlistsMediaId)
                    .setTitle("Playlists")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                builder.setMediaId(musicBrowserUtils.genresMediaId)
                    .setTitle("Genres")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            )
        )
    }

    /**
     * Get saved queue items, adding the category as all_songs (for simplicity. Could use another category name such as NOW_PLAYING)
     */
    fun getSavedQueueItems(): Single<List<MediaBrowserCompat.MediaItem>> {
        return sharedPreferencesRepo.getSavedQueueItems()
            .flatMapObservable { savedIds ->
                songsRepository.getSongsForIds(savedIds)
            }.map { it.mediaItems(musicBrowserUtils.allSongsMediaId) }
            .firstOrError()
    }

    fun getCurrentItem(): Single<NowPlayingItem> {
        return sharedPreferencesRepo.currentItemId()
    }

    fun saveCurrentItem(nowPlayingItem: NowPlayingItem) {
        sharedPreferencesRepo.saveCurrentItem(nowPlayingItem)
    }
}