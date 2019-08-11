package com.techbeloved.ogene.playback

import android.support.v4.media.session.MediaSessionCompat
import com.techbeloved.ogene.repo.models.NowPlayingItem

/**
 * A class for sending single playback request.
 * @param playWhenReady: indicates that playback should start as soon as player is ready
 */
data class PlayRequest(val item: MediaSessionCompat.QueueItem, val status: NowPlayingItem?, val playWhenReady: Boolean)