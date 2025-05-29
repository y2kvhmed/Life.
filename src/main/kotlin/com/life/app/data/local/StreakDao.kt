package com.life.app.data.local

import androidx.room.*
import com.life.app.data.model.Streak
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the streaks table.
 * This interface provides methods to interact with the streaks table in the Room database.
 */
@Dao
interface StreakDao {
    
    /**
     * Insert a new streak into the database.
     * If a streak with the same ID already exists, it will be replaced.
     * 
     * @param streak The streak to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: Streak)
    
    /**
     * Update an existing streak in the database.
     * 
     * @param streak The streak to update
     */
    @Update
    suspend fun updateStreak(streak: Streak)
    
    /**
     * Delete a streak from the database.
     * 
     * @param streak The streak to delete
     */
    @Delete
    suspend fun deleteStreak(streak: Streak)
    
    /**
     * Get a streak by its user ID.
     * 
     * @param userId The ID of the user whose streak to get
     * @return A Flow emitting the streak for the user, or null if no streak exists
     */
    @Query("SELECT * FROM streaks WHERE userId = :userId")
    fun getStreakByUserId(userId: String): Flow<Streak?>
    
    /**
     * Get all streaks from the database.
     * 
     * @return A Flow emitting the list of all streaks
     */
    @Query("SELECT * FROM streaks")
    fun getAllStreaks(): Flow<List<Streak>>
}