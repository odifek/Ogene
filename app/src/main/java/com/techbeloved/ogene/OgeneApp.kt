package com.techbeloved.ogene

import android.app.Application
import android.content.ComponentName

class OgeneApp : Application() {

    private lateinit var _appComponent: AppComponent
    val appComponent: AppComponent get() = _appComponent

    override fun onCreate() {
        super.onCreate()

        _appComponent = DaggerAppComponent.builder().context(this.applicationContext)
            .serviceComponent(ComponentName(this.applicationContext, MusicService::class.java))
            .build()
    }
}