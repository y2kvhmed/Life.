package com.life.app.di

import com.life.app.data.repository.*
import com.life.app.data.remote.DeepseekService
import com.life.app.ui.auth.AuthViewModel
import com.life.app.ui.coach.CoachViewModel
import com.life.app.ui.faith.FaithViewModel
import com.life.app.ui.home.HomeViewModel
import com.life.app.ui.journal.JournalEntryViewModel
import com.life.app.ui.journal.JournalViewModel
import com.life.app.ui.nutrition.NutritionViewModel
import com.life.app.ui.running.RunningViewModel
import com.life.app.ui.settings.SettingsViewModel
import com.life.app.ui.shares.SharesViewModel
import com.life.app.ui.workout.WorkoutViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Dagger Hilt module for providing ViewModel dependencies.
 */
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideHomeViewModel(
        userRepository: UserRepository,
        streakRepository: StreakRepository,
        workoutRepository: WorkoutRepository,
        runRepository: RunRepository,
        mealRepository: MealRepository,
        journalRepository: JournalRepository,
        motivationRepository: MotivationRepository,
        deepseekService: DeepseekService
    ): HomeViewModel {
        return HomeViewModel(
            userRepository = userRepository,
            streakRepository = streakRepository,
            workoutRepository = workoutRepository,
            runRepository = runRepository,
            mealRepository = mealRepository,
            journalRepository = journalRepository,
            motivationRepository = motivationRepository,
            deepseekService = deepseekService
        )
    }

    @Provides
    @ViewModelScoped
    fun provideAuthViewModel(
        userRepository: UserRepository
    ): AuthViewModel {
        return AuthViewModel(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCoachViewModel(
        deepseekService: DeepseekService,
        motivationRepository: MotivationRepository,
        planRepository: PlanRepository,
        workoutRepository: WorkoutRepository,
        mealRepository: MealRepository
    ): CoachViewModel {
        return CoachViewModel(
            deepseekService = deepseekService,
            motivationRepository = motivationRepository,
            planRepository = planRepository,
            workoutRepository = workoutRepository,
            mealRepository = mealRepository
        )
    }

    @Provides
    @ViewModelScoped
    fun provideWorkoutViewModel(
        workoutRepository: WorkoutRepository,
        streakRepository: StreakRepository,
        deepseekService: DeepseekService
    ): WorkoutViewModel {
        return WorkoutViewModel(
            workoutRepository = workoutRepository,
            streakRepository = streakRepository,
            deepseekService = deepseekService
        )
    }

    @Provides
    @ViewModelScoped
    fun provideRunningViewModel(
        runRepository: RunRepository,
        streakRepository: StreakRepository
    ): RunningViewModel {
        return RunningViewModel(
            runRepository = runRepository,
            streakRepository = streakRepository
        )
    }

    @Provides
    @ViewModelScoped
    fun provideNutritionViewModel(
        mealRepository: MealRepository,
        streakRepository: StreakRepository,
        deepseekService: DeepseekService
    ): NutritionViewModel {
        return NutritionViewModel(
            mealRepository = mealRepository,
            streakRepository = streakRepository,
            deepseekService = deepseekService
        )
    }

    @Provides
    @ViewModelScoped
    fun provideJournalViewModel(
        journalRepository: JournalRepository,
        deepseekService: DeepseekService
    ): JournalViewModel {
        return JournalViewModel(
            journalRepository = journalRepository,
            deepseekService = deepseekService
        )
    }

    @Provides
    @ViewModelScoped
    fun provideJournalEntryViewModel(
        journalRepository: JournalRepository,
        streakRepository: StreakRepository
    ): JournalEntryViewModel {
        return JournalEntryViewModel(
            journalRepository = journalRepository,
            streakRepository = streakRepository
        )
    }

    @Provides
    @ViewModelScoped
    fun provideFaithViewModel(
        prayerRepository: PrayerRepository,
        userRepository: UserRepository,
        deepseekService: DeepseekService
    ): FaithViewModel {
        return FaithViewModel(
            prayerRepository = prayerRepository,
            userRepository = userRepository,
            deepseekService = deepseekService
        )
    }

    @Provides
    @ViewModelScoped
    fun provideSharesViewModel(
        shareRepository: ShareRepository,
        userRepository: UserRepository
    ): SharesViewModel {
        return SharesViewModel(
            shareRepository = shareRepository,
            userRepository = userRepository
        )
    }

    @Provides
    @ViewModelScoped
    fun provideSettingsViewModel(
        userRepository: UserRepository
    ): SettingsViewModel {
        return SettingsViewModel(userRepository)
    }
}