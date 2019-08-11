package com.techbeloved.ogene.playback

import android.support.v4.media.session.MediaSessionCompat
import com.techbeloved.ogene.repo.models.NowPlayingItem

/**
 * A simple wrapper for saved queue items and current playing item data
 */
class SavedQueue(val queueItems: List<MediaSessionCompat.QueueItem>, val currentItem: NowPlayingItem)