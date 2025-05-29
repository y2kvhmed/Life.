package com.life.app.di

import com.life.app.util.ActivityChecker
import com.life.app.util.ActivityCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing ActivityChecker implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityCheckerModule {

    /**
     * Binds the ActivityCheckerImpl implementation to the ActivityChecker interface.
     */
    @Binds
    @Singleton
    abstract fun bindActivityChecker(activityCheckerImpl: ActivityCheckerImpl): ActivityChecker
}