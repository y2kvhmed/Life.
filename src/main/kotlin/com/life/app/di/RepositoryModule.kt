package com.life.app.di

import com.life.app.data.local.*
import com.life.app.data.remote.DeepseekService
import com.life.app.data.remote.SupabaseService
import com.life.app.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        supabaseService: SupabaseService
    ): UserRepository {
        return UserRepository(userDao, supabaseService)
    }

    @Provides
    @Singleton
    fun provideStreakRepository(
        streakDao: StreakDao,
        supabaseService: SupabaseService
    ): StreakRepository {
        return StreakRepository(streakDao, supabaseService)
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        workoutDao: WorkoutDao,
        supabaseService: SupabaseService,
        streakRepository: StreakRepository
    ): WorkoutRepository {
        return WorkoutRepository(workoutDao, supabaseService, streakRepository)
    }

    @Provides
    @Singleton
    fun provideRunRepository(
        runDao: RunDao,
        supabaseService: SupabaseService,
        streakRepository: StreakRepository
    ): RunRepository {
        return RunRepository(runDao, supabaseService, streakRepository)
    }

    @Provides
    @Singleton
    fun provideMealRepository(
        mealDao: MealDao,
        supabaseService: SupabaseService,
        streakRepository: StreakRepository
    ): MealRepository {
        return MealRepository(mealDao, supabaseService, streakRepository)
    }

    @Provides
    @Singleton
    fun provideJournalRepository(
        journalDao: JournalDao,
        supabaseService: SupabaseService,
        streakRepository: StreakRepository
    ): JournalRepository {
        return JournalRepository(journalDao, supabaseService, streakRepository)
    }

    @Provides
    @Singleton
    fun providePrayerRepository(
        prayerDao: PrayerDao,
        supabaseService: SupabaseService,
        streakRepository: StreakRepository,
        userRepository: UserRepository
    ): PrayerRepository {
        return PrayerRepository(prayerDao, supabaseService, streakRepository, userRepository)
    }

    @Provides
    @Singleton
    fun provideMotivationRepository(
        motivationDao: MotivationDao,
        supabaseService: SupabaseService,
        deepseekService: DeepseekService
    ): MotivationRepository {
        return MotivationRepository(motivationDao, supabaseService, deepseekService)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao,
        supabaseService: SupabaseService
    ): NoteRepository {
        return NoteRepository(noteDao, supabaseService)
    }

    @Provides
    @Singleton
    fun providePlanRepository(
        planDao: PlanDao,
        supabaseService: SupabaseService,
        deepseekService: DeepseekService
    ): PlanRepository {
        return PlanRepository(planDao, supabaseService, deepseekService)
    }

    @Provides
    @Singleton
    fun provideShareRepository(
        shareDao: ShareDao,
        commentDao: CommentDao,
        supabaseService: SupabaseService
    ): ShareRepository {
        return ShareRepository(shareDao, commentDao, supabaseService)
    }
}