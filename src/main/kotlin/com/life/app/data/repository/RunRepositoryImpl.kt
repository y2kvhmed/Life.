package com.life.app.data.repository

import com.life.app.data.local.RunDao
import com.life.app.data.model.Run
import com.life.app.data.remote.SupabaseService
import com.life.app.util.ActivityChecker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the RunRepository interface.
 * This class handles the data operations for runs, including local database operations and remote API calls.
 */
@Singleton
class RunRepositoryImpl @Inject constructor(
    private val runDao: RunDao,
    private val supabaseService: SupabaseService,
    private val streakRepository: StreakRepository,
    private val activityChecker: ActivityChecker
) : RunRepository {

    /**
     * Add a new run to the database and sync with Supabase.
     */
    override suspend fun addRun(run: Run): Long {
        // Save to local database
        val id = runDao.insertRun(run)
        
        // Try to save to Supabase
        try {
            supabaseService.createRun(run)
        } catch (e: Exception) {
            // Log error but don't block the local save
        }
        
        // Update streak
        val userId = run.userId
        if (userId.isNotEmpty()) {
            streakRepository.updateStreakForActivity(userId)
        }
        
        return id
    }

    /**
     * Get all runs for a user.
     */
    override fun getAllRunsForUser(userId: String): Flow<List<Run>> {
        return runDao.getAllRunsForUser(userId)
    }

    /**
     * Get runs for a specific date range.
     */
    override fun getRunsForDateRange(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Run>> {
        return runDao.getRunsForDateRange(userId, startDate, endDate)
    }

    /**
     * Get a specific run by ID.
     */
    override suspend fun getRunById(id: Long): Run? {
        return runDao.getRunById(id)
    }

    /**
     * Update an existing run.
     */
    override suspend fun updateRun(run: Run) {
        // Update in local database
        runDao.updateRun(run)
        
        // Try to update in Supabase
        try {
            supabaseService.updateRun(run)
        } catch (e: Exception) {
            // Log error but don't block the local update
        }
    }

    /**
     * Delete a run.
     */
    override suspend fun deleteRun(run: Run) {
        // Delete from local database
        runDao.deleteRun(run)
        
        // Try to delete from Supabase
        try {
            supabaseService.deleteRun(run.id.toString())
        } catch (e: Exception) {
            // Log error but don't block the local delete
        }
        
        // Check if we need to update streak
        val userId = run.userId
        if (userId.isNotEmpty()) {
            val today = LocalDateTime.now()
            val startOfDay = today.withHour(0).withMinute(0).withSecond(0)
            val endOfDay = today.withHour(23).withMinute(59).withSecond(59)
            
            // If the deleted run was from today, check if there are any other activities today
            if (run.date.isAfter(startOfDay) && run.date.isBefore(endOfDay)) {
                val hasOtherRunsToday = getRunsForDateRange(userId, startOfDay, endOfDay).first().isNotEmpty()
                
                if (!hasOtherRunsToday) {
                    // Check if there are any other activities today using the ActivityChecker
                val hasOtherActivitiesToday = activityChecker.hasActivitiesForDate(userId, LocalDate.now())
                
                // Only reset streak if there are no other activities today
                if (!hasOtherActivitiesToday) {
                    streakRepository.checkAndResetStreakIfNeeded(userId)
                }
                }
            }
        }
    }

    /**
     * Get statistics for a user's runs.
     */
    override suspend fun getRunStats(userId: String): RunStats {
        val totalDistance = runDao.getTotalDistanceForUser(userId) ?: 0f
        val totalDuration = runDao.getTotalDurationForUser(userId) ?: 0L
        val totalRuns = runDao.getRunCountForUser(userId)
        val avgSpeed = runDao.getAverageSpeedForUser(userId) ?: 0f
        val totalCalories = runDao.getTotalCaloriesBurnedForUser(userId) ?: 0
        
        return RunStats(
            totalRuns = totalRuns,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            averageSpeed = avgSpeed,
            totalCaloriesBurned = totalCalories
        )
    }

    /**
     * Sync runs with Supabase.
     */
    override suspend fun syncWithRemote(userId: String) {
        try {
            // Get runs from Supabase
            val remoteRuns = supabaseService.getRunsByUserId(userId)
            
            // Get local runs
            val localRuns = runDao.getAllRunsForUserAsList(userId)
            
            // Process remote runs that aren't in local database
            remoteRuns.forEach { remoteRun ->
                val localRun = localRuns.find { it.id.toString() == remoteRun.id }
                
                if (localRun == null) {
                    // Remote run not in local database, add it
                    runDao.insertRun(remoteRun)
                }
                // Note: We're not handling conflicts here for simplicity
                // In a real app, you would need to decide which version to keep
            }
            
            // Process local runs that aren't in remote database
            localRuns.forEach { localRun ->
                val remoteRun = remoteRuns.find { it.id.toString() == localRun.id.toString() }
                
                if (remoteRun == null) {
                    // Local run not in remote database, add it
                    supabaseService.createRun(localRun)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}

/**
 * Data class for run statistics.
 */
data class RunStats(
    val totalRuns: Int,
    val totalDistance: Float, // in meters
    val totalDuration: Long, // in milliseconds
    val averageSpeed: Float, // in km/h
    val totalCaloriesBurned: Int
)