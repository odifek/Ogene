package com.techbeloved.ogene

import android.app.Application
import android.content.ComponentName
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences

class OgeneApp : Application() {

    private lateinit var _appComponent: AppComponent
    val appComponent: AppComponent get() = _appComponent

    override fun onCreate() {
        super.onCreate()

        _appComponent = DaggerAppComponent.builder().context(this.applicationContext)
            .serviceComponent(ComponentName(this.applicationContext, MusicService::class.java))
            .rxPreferences(RxSharedPreferences.create(PreferenceManager.getDefaultSharedPreferences(this.applicationContext)))
            .build()
    }
}