package com.techbeloved.ogene.playback

import android.support.v4.media.MediaBrowserCompat
import com.techbeloved.ogene.MusicService
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope
import javax.inject.Singleton

@Singleton
@Subcomponent(modules = [PlaybackModule::class])
interface PlaybackComponent {

    fun inject(service: MusicService)

    @Subcomponent.Builder
    interface Builder {
        fun build(): PlaybackComponent

        @BindsInstance
        fun mediaBrowser(mediaBrowser: MediaBrowserCompat): Builder
    }

    @Module(subcomponents = [PlaybackComponent::class])
    interface InstallationModule
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceScope