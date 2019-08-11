package com.techbeloved.ogene.playback

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

private val MediaSessionCompat.QueueItem.toMetadata: MediaMetadataCompat
    get() {
        val description = this.description
        return MediaMetadataCompat.Builder()
            .apply {
                putText(MediaMetadataCompat.METADATA_KEY_TITLE, description.title)
                putText(MediaMetadataCompat.METADATA_KEY_ALBUM, description.subtitle)
                putText(MediaMetadataCompat.METADATA_KEY_ARTIST, description.description)
                putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, description.iconBitmap)
            }
            .build()
    }


class MediaPlayerAdapter @Inject constructor(private val context: Context) : Playback {

    private var currentPlayer: MediaPlayer? = null
    private var nextPlayer: MediaPlayer? = null

    private var playerReady: Boolean = false

    private val playbackStatusSubject = PublishRelay.create<PlaybackStatus>()

    override fun prepare(mediaUri: String, ready: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playWhenReady(request: PlayRequest): Observable<PlaybackStatus> {
        return Observable.create { emitter ->

            emitter.setCancellable { releaseCurrent() }

            playerReady = false
            initializeCurrentPlayer()
            Timber.i("QueueItem, %s", request.item.queueId)
            val mediaUri =
                request.item.description.mediaUri
                    ?: throw Throwable("Media Uri should not be null!")
            if (mediaUri.scheme?.startsWith("content") == true) {
                try {
                    currentPlayer?.setDataSource(context, mediaUri)
                } catch (e: IOException) {
                    emitter.tryOnError(Throwable("Unable to open file", e))
                } catch (e: IllegalArgumentException) {
                    emitter.tryOnError(Throwable("Invalid uri supplied", e))
                } catch (e: IllegalStateException) {
                    emitter.tryOnError(Throwable("Media player not ready!", e))
                } catch (e: SecurityException) {
                    emitter.tryOnError(
                        Throwable(
                            "You don't have permission " +
                                    "to access this file!", e
                        )
                    )
                }
            } else {
                try {
                    currentPlayer?.setDataSource(mediaUri.encodedPath)
                } catch (e: IOException) {
                    emitter.tryOnError(Throwable("Unable to open file", e))
                } catch (e: IllegalArgumentException) {
                    emitter.tryOnError(Throwable("Invalid uri supplied", e))
                } catch (e: IllegalStateException) {
                    emitter.tryOnError(Throwable("Media player not ready!", e))
                } catch (e: SecurityException) {
                    emitter.tryOnError(
                        Throwable(
                            "You don't have permission " +
                                    "to access this file!", e
                        )
                    )
                }
            }

            if (!emitter.isDisposed) {
                try {
                    currentPlayer?.prepare()
                    request.status?.let {
                        currentPlayer?.seekTo(it.playbackPos.toInt())
                    }
                    emitter.onNext(PlaybackStatus.Started(request.item.toMetadata, duration()))
                    if (request.playWhenReady) {
                        start(false)
                    }
                    playerReady = true
                } catch (e: IllegalStateException) {
                    emitter.tryOnError(Throwable("Media player not ready!", e))
                } catch (e: IOException) {
                    emitter.tryOnError(Throwable("Unable to open file", e))
                }
            }

        }
    }

    override fun start(checkPlayerStatus: Boolean) {
        if (checkPlayerStatus) {
            if (playerReady) {
                currentPlayer?.let { player ->
                    try {
                        if (!player.isPlaying) {
                            player.start()
                        }
                        player.setVolume(1.0f, 1.0f)
                        broadcastStatus(
                            PlaybackStatus.Playing(
                                player.duration,
                                player.currentPosition
                            )
                        )
                    } catch (e: IllegalStateException) {
                        broadcastStatus(
                            PlaybackStatus.Error(
                                "Player accessed in invalid state!",
                                false
                            )
                        )
                    }
                }
            }
        } else {
            currentPlayer?.let { player ->
                player.start()
                player.setVolume(1.0f, 1.0f)
                broadcastStatus(PlaybackStatus.Playing(player.duration, player.currentPosition))
            }
        }
    }

    override fun pause() {
        if (playerReady) {
            currentPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.pause()
                        broadcastStatus(
                            PlaybackStatus.Paused(
                                player.duration,
                                player.currentPosition
                            )
                        )
                    }
                } catch (e: java.lang.IllegalStateException) {
                    broadcastStatus(
                        PlaybackStatus.Error(
                            "Media Player accessed in invalid state!",
                            true
                        )
                    )
                }

            }
        }
    }

    override fun stop() {
        if (playerReady) {
            currentPlayer?.let { player ->
                try {
                    player.stop()
                    broadcastStatus(PlaybackStatus.Stopped)
                    releaseCurrent()
                } catch (e: java.lang.IllegalStateException) {
                    PlaybackStatus.Error(
                        "Media Player accessed in invalid state!",
                        true
                    )
                }
            }
        }
    }

    override fun duck() {
        if (playerReady) {
            currentPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.setVolume(0.3f, 0.3f)
                        broadcastStatus(
                            PlaybackStatus.Ducked(
                                player.duration,
                                player.currentPosition
                            )
                        )
                    }
                } catch (e: java.lang.IllegalStateException) {
                    broadcastStatus(
                        PlaybackStatus.Error(
                            "Media Player accessed in invalid state!",
                            true
                        )
                    )
                }

            }
        }
    }

    override fun isPlaying(): Boolean {
        return currentPlayer?.isPlaying ?: false
    }

    override fun duration(): Int {
        return currentPlayer?.duration ?: -1
    }

    override fun position(): Int {
        return currentPlayer?.currentPosition ?: -1
    }

    override fun isReady(): Boolean = playerReady

    private fun initializeCurrentPlayer() {
        if (currentPlayer == null) {
            currentPlayer = MediaPlayer()
        } else {
            currentPlayer?.reset()
        }

        currentPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

        currentPlayer?.setOnErrorListener { mp, what, extra ->
            when (what) {
                MediaPlayer.MEDIA_ERROR_UNKNOWN -> {
                    broadcastStatus(resolveError(extra, true))
                    true
                }
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                    broadcastStatus(resolveError(what, false))
                    true
                }
                else -> false
            }

        }

        currentPlayer?.setOnCompletionListener { mp ->
            broadcastStatus(PlaybackStatus.Completed)
        }
    }

    private fun resolveError(errorCode: Int, recoverable: Boolean): PlaybackStatus.Error {
        return when (errorCode) {
            MediaPlayer.MEDIA_ERROR_IO -> PlaybackStatus.Error("IO Error!", recoverable)
            MediaPlayer.MEDIA_ERROR_MALFORMED -> PlaybackStatus.Error(
                "Malformed stream",
                recoverable
            )
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> PlaybackStatus.Error(
                "Media not supported",
                recoverable
            )
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> PlaybackStatus.Error(
                "Operation timed out!",
                recoverable
            )
            else -> PlaybackStatus.Error("Other error $errorCode", recoverable)
        }
    }

    private fun releaseCurrent() {
        currentPlayer?.release()
        currentPlayer = null
        playerReady = false
    }

    private fun broadcastStatus(status: PlaybackStatus) {
        playbackStatusSubject.accept(status)
    }

    override fun playbackStatus(): Observable<PlaybackStatus> {
        return playbackStatusSubject.hide()
    }
}