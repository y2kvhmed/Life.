package com.life.app.data.local

import androidx.room.*
import com.life.app.data.model.Run
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for the runs table.
 * This interface provides methods to interact with the runs table in the Room database.
 */
@Dao
interface RunDao {
    
    /**
     * Insert a new run into the database.
     * If a run with the same ID already exists, it will be replaced.
     * 
     * @param run The run to insert
     * @return The row ID of the inserted run
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run): Long
    
    /**
     * Insert multiple runs into the database.
     * If a run with the same ID already exists, it will be replaced.
     * 
     * @param runs The list of runs to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuns(runs: List<Run>)
    
    /**
     * Update an existing run in the database.
     * 
     * @param run The run to update
     */
    @Update
    suspend fun updateRun(run: Run)
    
    /**
     * Delete a run from the database.
     * 
     * @param run The run to delete
     */
    @Delete
    suspend fun deleteRun(run: Run)
    
    /**
     * Get a run by its ID.
     * 
     * @param id The ID of the run to get
     * @return The run with the given ID, or null if no such run exists
     */
    @Query("SELECT * FROM runs WHERE id = :id")
    suspend fun getRunById(id: Long): Run?
    
    /**
     * Get all runs for a specific user, ordered by date (newest first).
     * 
     * @param userId The ID of the user whose runs to get
     * @return A Flow emitting the list of runs for the user
     */
    @Query("SELECT * FROM runs WHERE userId = :userId ORDER BY date DESC")
    fun getAllRunsForUser(userId: String): Flow<List<Run>>
    
    /**
     * Get all runs for a specific user as a list (not a Flow).
     * This is useful for one-time operations like syncing with the remote database.
     * 
     * @param userId The ID of the user whose runs to get
     * @return The list of runs for the user
     */
    @Query("SELECT * FROM runs WHERE userId = :userId")
    suspend fun getAllRunsForUserAsList(userId: String): List<Run>
    
    /**
     * Get runs for a specific date range, ordered by date (newest first).
     * 
     * @param userId The ID of the user whose runs to get
     * @param startDate The start of the date range
     * @param endDate The end of the date range
     * @return A Flow emitting the list of runs in the date range
     */
    @Query("SELECT * FROM runs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRunsForDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Run>>
    
    /**
     * Get the total distance run by a user.
     * 
     * @param userId The ID of the user
     * @return The total distance in meters
     */
    @Query("SELECT SUM(distanceInMeters) FROM runs WHERE userId = :userId")
    suspend fun getTotalDistanceForUser(userId: String): Float?
    
    /**
     * Get the total duration of all runs by a user.
     * 
     * @param userId The ID of the user
     * @return The total duration in milliseconds
     */
    @Query("SELECT SUM(durationInMillis) FROM runs WHERE userId = :userId")
    suspend fun getTotalDurationForUser(userId: String): Long?
    
    /**
     * Get the average speed of all runs by a user.
     * 
     * @param userId The ID of the user
     * @return The average speed in kilometers per hour
     */
    @Query("SELECT AVG(avgSpeedInKMH) FROM runs WHERE userId = :userId")
    suspend fun getAverageSpeedForUser(userId: String): Float?
    
    /**
     * Get the total calories burned by a user.
     * 
     * @param userId The ID of the user
     * @return The total calories burned
     */
    @Query("SELECT SUM(caloriesBurned) FROM runs WHERE userId = :userId")
    suspend fun getTotalCaloriesBurnedForUser(userId: String): Int?
    
    /**
     * Get the count of runs for a user.
     * 
     * @param userId The ID of the user
     * @return The number of runs
     */
    @Query("SELECT COUNT(*) FROM runs WHERE userId = :userId")
    suspend fun getRunCountForUser(userId: String): Int
    
    /**
     * Get the count of runs for a user in a specific date range.
     * 
     * @param userId The ID of the user
     * @param startDate The start of the date range
     * @param endDate The end of the date range
     * @return The number of runs in the date range
     */
    @Query("SELECT COUNT(*) FROM runs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getRunCountForDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Int
}