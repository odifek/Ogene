package com.techbeloved.ogene.schedulers

import dagger.Binds
import dagger.Module

@Module
interface SchedulerModule {
    @Binds
    fun provideSchedulers(schedulerProviderImp: SchedulerProviderImp): SchedulerProvider
}