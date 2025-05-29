package com.life.app.data.repository

import com.life.app.data.local.WorkoutDao
import com.life.app.data.model.Workout
import com.life.app.data.model.WorkoutType
import com.life.app.data.remote.SupabaseService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for workout-related data operations.
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val supabaseService: SupabaseService,
    private val streakRepository: StreakRepository
) {

    /**
     * Get all workouts for a user.
     */
    fun getWorkouts(userId: String): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByUserId(userId)
    }

    /**
     * Get a specific workout by ID.
     */
    fun getWorkout(workoutId: String): Flow<Workout?> {
        return workoutDao.getWorkoutById(workoutId)
    }

    /**
     * Get workouts for a specific date range.
     */
    fun getWorkoutsForDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByDateRange(userId, startDate, endDate)
    }

    /**
     * Create a new workout and update the user's streak.
     */
    suspend fun createWorkout(userId: String, name: String, type: WorkoutType, duration: Int, difficulty: Int, notes: String? = null) {
        val workout = Workout(
            id = supabaseService.generateId(),
            userId = userId,
            name = name,
            type = type,
            duration = duration,
            difficulty = difficulty,
            notes = notes
        )
        workoutDao.insertWorkout(workout)
        supabaseService.createWorkout(workout)
        
        // Update streak when a workout is logged
        streakRepository.updateStreakForActivity(userId)
    }

    /**
     * Update an existing workout.
     */
    suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout)
        supabaseService.updateWorkout(workout)
    }

    /**
     * Delete a workout.
     */
    suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout)
        supabaseService.deleteWorkout(workout.id)
    }

    /**
     * Sync workouts from the remote database to the local database.
     */
    suspend fun syncWorkouts(userId: String) {
        val remoteWorkouts = supabaseService.getWorkoutsByUserId(userId)
        remoteWorkouts.forEach { workout ->
            workoutDao.insertWorkout(workout)
        }
    }
}