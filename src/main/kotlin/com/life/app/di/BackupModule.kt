package com.life.app.di

import com.life.app.data.repository.BackupRepository
import com.life.app.data.repository.BackupRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing BackupRepository implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {

    /**
     * Binds the BackupRepositoryImpl implementation to the BackupRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindBackupRepository(backupRepositoryImpl: BackupRepositoryImpl): BackupRepository
}