package com.techbeloved.ogene.playback

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class PlaybackModule {

    @Singleton
    @Binds
    abstract fun providesPlayback(playback: MediaPlayerAdapter): Playback

    @Singleton
    @Binds
    abstract fun providesQueueManager(queueManagerImp: QueueManagerImp): QueueManager
}