package com.techbeloved.ogene.repo

import com.techbeloved.ogene.repo.models.Song

interface SongsRepository {

    fun numberOfSongs(): Int

    fun getSongAtPosition(position: Int): Song?

    fun getSongsAtRange(startPosition: Int, endPosition: Int): List<Song>

    fun getAllSongs(): List<Song>
}