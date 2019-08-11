package com.techbeloved.ogene.repo.models

/**
 * A simple data class for storing the state of a now playing item.
 * It doesn't contain the entire information, but just the id, playback position and duration
 */
data class NowPlayingItem(val id: Long, val playbackPos: Long, val duration: Long)