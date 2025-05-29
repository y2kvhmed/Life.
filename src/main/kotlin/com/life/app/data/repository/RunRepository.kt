package com.life.app.data.repository

import com.life.app.data.model.Run
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository interface for run-related data operations.
 */
interface RunRepository {
    
    /**
     * Add a new run to the database and sync with Supabase.
     * 
     * @param run The run to add
     * @return The ID of the newly added run
     */
    suspend fun addRun(run: Run): Long
    
    /**
     * Get all runs for a user.
     * 
     * @param userId The ID of the user whose runs to get
     * @return A Flow emitting the list of runs for the user
     */
    fun getAllRunsForUser(userId: String): Flow<List<Run>>
    
    /**
     * Get runs for a specific date range.
     * 
     * @param userId The ID of the user whose runs to get
     * @param startDate The start of the date range
     * @param endDate The end of the date range
     * @return A Flow emitting the list of runs in the date range
     */
    fun getRunsForDateRange(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Run>>
    
    /**
     * Get a specific run by ID.
     * 
     * @param id The ID of the run to get
     * @return The run with the given ID, or null if no such run exists
     */
    suspend fun getRunById(id: Long): Run?
    
    /**
     * Update an existing run.
     * 
     * @param run The run to update
     */
    suspend fun updateRun(run: Run)
    
    /**
     * Delete a run.
     * 
     * @param run The run to delete
     */
    suspend fun deleteRun(run: Run)
    
    /**
     * Get statistics for a user's runs.
     * 
     * @param userId The ID of the user whose statistics to get
     * @return The run statistics for the user
     */
    suspend fun getRunStats(userId: String): RunStats
    
    /**
     * Sync runs with Supabase.
     * 
     * @param userId The ID of the user whose runs to sync
     */
    suspend fun syncWithRemote(userId: String)
}