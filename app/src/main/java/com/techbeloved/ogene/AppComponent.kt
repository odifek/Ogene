package com.techbeloved.ogene

import android.content.ComponentName
import android.content.Context
import com.techbeloved.ogene.musicbrowser.ViewModelModule
import com.techbeloved.ogene.playback.PlaybackModule
import com.techbeloved.ogene.repo.SongsRepositoryModule
import com.techbeloved.ogene.schedulers.SchedulerModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules =  [ViewModelModule::class, SongsRepositoryModule::class, SchedulerModule::class, PlaybackModule::class])
interface AppComponent {
    fun inject(musicService: MusicService)

    fun inject(mainActivity: MainActivity)

    @Component.Builder
    interface Builder {
        fun build(): AppComponent

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun serviceComponent(componentName: ComponentName): Builder
    }
}