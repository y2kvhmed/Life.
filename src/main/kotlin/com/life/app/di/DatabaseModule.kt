package com.life.app.di

import android.content.Context
import androidx.room.Room
import com.life.app.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLifeDatabase(@ApplicationContext context: Context): LifeDatabase {
        return Room.databaseBuilder(
            context,
            LifeDatabase::class.java,
            "life_database"
        )
        .fallbackToDestructiveMigration() // For simplicity in development
        .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: LifeDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(database: LifeDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    @Singleton
    fun provideRunDao(database: LifeDatabase): RunDao {
        return database.runDao()
    }

    @Provides
    @Singleton
    fun provideMealDao(database: LifeDatabase): MealDao {
        return database.mealDao()
    }

    @Provides
    @Singleton
    fun provideWaterIntakeDao(database: LifeDatabase): WaterIntakeDao {
        return database.waterIntakeDao()
    }

    @Provides
    @Singleton
    fun provideJournalDao(database: LifeDatabase): JournalDao {
        return database.journalDao()
    }

    @Provides
    @Singleton
    fun provideMotivationDao(database: LifeDatabase): MotivationDao {
        return database.motivationDao()
    }

    @Provides
    @Singleton
    fun providePrayerDao(database: LifeDatabase): PrayerDao {
        return database.prayerDao()
    }

    @Provides
    @Singleton
    fun provideStreakDao(database: LifeDatabase): StreakDao {
        return database.streakDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: LifeDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun providePlanDao(database: LifeDatabase): PlanDao {
        return database.planDao()
    }

    @Provides
    @Singleton
    fun provideShareDao(database: LifeDatabase): ShareDao {
        return database.shareDao()
    }

    @Provides
    @Singleton
    fun provideCommentDao(database: LifeDatabase): CommentDao {
        return database.commentDao()
    }
}