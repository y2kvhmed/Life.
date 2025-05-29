package com.life.app.data.repository

import com.life.app.data.local.MotivationDao
import com.life.app.data.model.Motivation
import com.life.app.data.remote.SupabaseService
import com.life.app.data.remote.DeepseekService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing motivational content.
 */
@Singleton
class MotivationRepository @Inject constructor(
    private val motivationDao: MotivationDao,
    private val supabaseService: SupabaseService,
    private val deepseekService: DeepseekService
) {

    /**
     * Get all motivational messages for a user.
     */
    fun getAllMotivations(userId: String): Flow<List<Motivation>> {
        return motivationDao.getAllMotivationsForUser(userId)
    }

    /**
     * Get the latest motivational message for a user.
     */
    fun getLatestMotivation(userId: String): Flow<Motivation?> {
        return motivationDao.getLatestMotivation(userId)
    }

    /**
     * Get a specific motivational message by ID.
     */
    suspend fun getMotivationById(motivationId: String): Motivation? {
        return motivationDao.getMotivationById(motivationId)
    }

    /**
     * Create a new motivational message.
     */
    suspend fun createMotivation(
        userId: String,
        content: String
    ): Motivation {
        val motivation = Motivation(
            id = generateMotivationId(),
            userId = userId,
            content = content,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save locally
        motivationDao.insertMotivation(motivation)

        // Save to Supabase
        try {
            supabaseService.createMotivation(motivation)
        } catch (e: Exception) {
            // Handle error, but don't block the local save
        }

        return motivation
    }

    /**
     * Update an existing motivational message.
     */
    suspend fun updateMotivation(motivation: Motivation): Motivation {
        val updatedMotivation = motivation.copy(updatedAt = LocalDateTime.now())

        // Update locally
        motivationDao.updateMotivation(updatedMotivation)

        // Update in Supabase
        try {
            supabaseService.updateMotivation(updatedMotivation)
        } catch (e: Exception) {
            // Handle error, but don't block the local update
        }

        return updatedMotivation
    }

    /**
     * Delete a motivational message.
     */
    suspend fun deleteMotivation(motivation: Motivation) {
        // Delete locally
        motivationDao.deleteMotivation(motivation)

        // Delete from Supabase
        try {
            supabaseService.deleteMotivation(motivation.id)
        } catch (e: Exception) {
            // Handle error, but don't block the local delete
        }
    }

    /**
     * Generate a new motivational message using Deepseek AI.
     */
    suspend fun generateMotivationalMessage(
        userId: String,
        mood: String? = null,
        context: String? = null
    ): String {
        try {
            // Get user streak for context
            val streak = null // Replace with actual streak from StreakRepository
            
            // Generate motivational message using Deepseek AI
            val message = deepseekService.generateMotivationalMessage(
                streak = streak,
                recentActivity = false,
                mood = mood
            )
            
            // Create and save the motivation
            createMotivation(userId, message)
            
            return message
        } catch (e: Exception) {
            // If AI generation fails, return a default message
            val defaultMessage = "Every step you take brings you closer to your goals. Keep going!"
            createMotivation(userId, defaultMessage)
            return defaultMessage
        }
    }

    /**
     * Get a curated list of motivational messages.
     */
    fun getCuratedMotivationalMessages(): List<String> {
        return listOf(
            "The only way to do great work is to love what you do.",
            "It does not matter how slowly you go as long as you do not stop.",
            "Believe you can and you're halfway there.",
            "Your only limit is your mind.",
            "The harder you work for something, the greater you'll feel when you achieve it.",
            "Don't watch the clock; do what it does. Keep going.",
            "Success is not final, failure is not fatal: It is the courage to continue that counts.",
            "The future belongs to those who believe in the beauty of their dreams.",
            "You are never too old to set another goal or to dream a new dream.",
            "The only person you are destined to become is the person you decide to be."
        )
    }

    /**
     * Sync motivations with Supabase.
     */
    suspend fun syncWithRemote(userId: String) {
        try {
            // Get motivations from Supabase
            val remoteMotivations = supabaseService.getMotivationsForUser(userId)
            
            // Get local motivations
            val localMotivations = motivationDao.getAllMotivationsForUserAsList(userId)
            
            // Find motivations that are in remote but not in local
            val motivationsToAdd = remoteMotivations.filter { remoteMotivation ->
                localMotivations.none { it.id == remoteMotivation.id }
            }
            
            // Find motivations that are in local but not in remote
            val motivationsToUpload = localMotivations.filter { localMotivation ->
                remoteMotivations.none { it.id == localMotivation.id }
            }
            
            // Find motivations that are in both but might have different data
            val motivationsToUpdate = localMotivations.filter { localMotivation ->
                remoteMotivations.any { it.id == localMotivation.id && it.updatedAt != localMotivation.updatedAt }
            }
            
            // Add remote motivations to local
            if (motivationsToAdd.isNotEmpty()) {
                motivationDao.insertMotivations(motivationsToAdd)
            }
            
            // Upload local motivations to remote
            motivationsToUpload.forEach { motivation ->
                supabaseService.createMotivation(motivation)
            }
            
            // Update motivations that are different
            motivationsToUpdate.forEach { localMotivation ->
                val remoteMotivation = remoteMotivations.first { it.id == localMotivation.id }
                
                // Use the most recent version
                if (localMotivation.updatedAt.isAfter(remoteMotivation.updatedAt)) {
                    supabaseService.updateMotivation(localMotivation)
                } else {
                    motivationDao.updateMotivation(remoteMotivation)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Generate a unique ID for a new motivational message.
     */
    private fun generateMotivationId(): String {
        return "motivation_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}