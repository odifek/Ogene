package com.techbeloved.ogene.schedulers

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

interface SchedulerProvider {
    fun ui(): Scheduler
    fun io(): Scheduler
}

class ImmediateSchedulerProviders: SchedulerProvider {
    override fun ui(): Scheduler = Schedulers.trampoline()

    override fun io(): Scheduler = Schedulers.trampoline()
}

@Singleton
class SchedulerProviderImp @Inject constructor (): SchedulerProvider {
    override fun ui(): Scheduler = AndroidSchedulers.mainThread()

    override fun io() = Schedulers.io()

}