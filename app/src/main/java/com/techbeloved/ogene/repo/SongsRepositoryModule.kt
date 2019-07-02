package com.techbeloved.ogene.repo

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface SongsRepositoryModule {
    @Singleton
    @Binds
    fun provideSongsRepository(songsRepo: CursorBasedSongsRepository): SongsRepository

}