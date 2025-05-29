package com.life.app.data.repository

import com.life.app.data.local.StreakDao
import com.life.app.data.model.Streak
import com.life.app.data.remote.SupabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for streak-related data operations.
 */
@Singleton
class StreakRepository @Inject constructor(
    private val streakDao: StreakDao,
    private val supabaseService: SupabaseService
) {

    /**
     * Get the current streak for a user.
     */
    fun getUserStreak(userId: String): Flow<Streak?> {
        return streakDao.getStreakByUserId(userId)
    }

    /**
     * Update the user's streak when an activity is logged.
     */
    suspend fun updateStreakForActivity(userId: String) {
        val currentStreak = streakDao.getStreakByUserId(userId).first()
        val now = LocalDateTime.now()
        
        if (currentStreak == null) {
            // First activity ever, create a new streak
            val newStreak = Streak(
                id = supabaseService.generateId(),
                userId = userId,
                updatedAt = now,
                count = 1
            )
            streakDao.insertStreak(newStreak)
            supabaseService.createStreak(newStreak)
        } else {
            val daysSinceLastActivity = ChronoUnit.DAYS.between(currentStreak.updatedAt, now)
            
            when {
                daysSinceLastActivity == 0L -> {
                    // Activity on the same day, no streak change needed
                    val updatedStreak = currentStreak.copy(updatedAt = now)
                    streakDao.updateStreak(updatedStreak)
                    supabaseService.updateStreak(updatedStreak)
                }
                daysSinceLastActivity == 1L -> {
                    // Activity on consecutive day, increment streak
                    val updatedStreak = currentStreak.copy(
                        updatedAt = now,
                        count = currentStreak.count + 1
                    )
                    streakDao.updateStreak(updatedStreak)
                    supabaseService.updateStreak(updatedStreak)
                }
                else -> {
                    // Activity after a gap, reset streak to 1
                    val updatedStreak = currentStreak.copy(
                        updatedAt = now,
                        count = 1
                    )
                    streakDao.updateStreak(updatedStreak)
                    supabaseService.updateStreak(updatedStreak)
                }
            }
        }
    }

    /**
     * Check if the streak needs to be reset due to inactivity.
     */
    suspend fun checkAndResetStreakIfNeeded(userId: String) {
        val currentStreak = streakDao.getStreakByUserId(userId).first() ?: return
        val now = LocalDateTime.now()
        val daysSinceLastActivity = ChronoUnit.DAYS.between(currentStreak.updatedAt, now)
        
        if (daysSinceLastActivity > 1) {
            // More than one day of inactivity, reset streak to 0
            val updatedStreak = currentStreak.copy(
                updatedAt = now,
                count = 0
            )
            streakDao.updateStreak(updatedStreak)
            supabaseService.updateStreak(updatedStreak)
        }
    }

    /**
     * Sync streak data from the remote database to the local database.
     */
    suspend fun syncStreak(userId: String) {
        val remoteStreak = supabaseService.getStreakByUserId(userId)
        if (remoteStreak != null) {
            streakDao.insertStreak(remoteStreak)
        }
    }
}