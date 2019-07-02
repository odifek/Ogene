package com.techbeloved.ogene.repo

import com.techbeloved.ogene.repo.models.Song
import io.reactivex.Observable
import io.reactivex.Single

interface SongsRepository {

    fun numberOfSongs(): Int

    fun getSongAtPosition(position: Int): Song?

    fun getSongsAtRange(startPosition: Int, endPosition: Int): Single<List<Song>>

    fun getAllSongs(): Single<List<Song>>
}