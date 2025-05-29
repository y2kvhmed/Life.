package com.life.app.data.repository

import com.life.app.data.local.JournalDao
import com.life.app.data.model.Journal
import com.life.app.data.remote.SupabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing journal entries.
 */
@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao,
    private val supabaseService: SupabaseService,
    private val streakRepository: StreakRepository
) {

    /**
     * Get all journal entries for a user.
     */
    fun getAllJournals(userId: String): Flow<List<Journal>> {
        return journalDao.getAllJournalsForUser(userId)
    }

    /**
     * Get journal entries for a specific date range.
     */
    fun getJournalsForDateRange(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Journal>> {
        return journalDao.getJournalsForDateRange(userId, startDate, endDate)
    }

    /**
     * Get journal entries with a specific mood.
     */
    fun getJournalsByMood(userId: String, mood: String): Flow<List<Journal>> {
        return journalDao.getJournalsByMood(userId, mood)
    }

    /**
     * Get journal entries with specific tags.
     */
    fun getJournalsByTags(userId: String, tags: List<String>): Flow<List<Journal>> {
        return journalDao.getJournalsByTags(userId, tags)
    }

    /**
     * Get a specific journal entry by ID.
     */
    suspend fun getJournalById(journalId: String): Journal? {
        return journalDao.getJournalById(journalId)
    }

    /**
     * Create a new journal entry.
     */
    suspend fun createJournal(
        userId: String,
        content: String,
        mood: String? = null,
        tags: List<String>? = null,
        isEncrypted: Boolean = false
    ): Journal {
        val journal = Journal(
            id = generateJournalId(),
            userId = userId,
            content = content,
            mood = mood,
            tags = tags,
            isEncrypted = isEncrypted,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save locally
        journalDao.insertJournal(journal)

        // Save to Supabase
        try {
            supabaseService.createJournal(journal)
        } catch (e: Exception) {
            // Handle error, but don't block the local save
        }

        // Update streak
        streakRepository.updateStreakForActivity(userId)

        return journal
    }

    /**
     * Update an existing journal entry.
     */
    suspend fun updateJournal(journal: Journal): Journal {
        val updatedJournal = journal.copy(updatedAt = LocalDateTime.now())

        // Update locally
        journalDao.updateJournal(updatedJournal)

        // Update in Supabase
        try {
            supabaseService.updateJournal(updatedJournal)
        } catch (e: Exception) {
            // Handle error, but don't block the local update
        }

        return updatedJournal
    }

    /**
     * Delete a journal entry.
     */
    suspend fun deleteJournal(journal: Journal) {
        // Delete locally
        journalDao.deleteJournal(journal)

        // Delete from Supabase
        try {
            supabaseService.deleteJournal(journal.id)
        } catch (e: Exception) {
            // Handle error, but don't block the local delete
        }

        // Check if we need to update streak
        val userId = journal.userId
        val today = LocalDateTime.now()
        val startOfDay = today.withHour(0).withMinute(0).withSecond(0)
        val endOfDay = today.withHour(23).withMinute(59).withSecond(59)

        // If the deleted journal was from today, check if there are any other activities today
        if (journal.createdAt.isAfter(startOfDay) && journal.createdAt.isBefore(endOfDay)) {
            val hasOtherJournalsToday = getJournalsForDateRange(userId, startOfDay, endOfDay).first().isNotEmpty()
            
            if (!hasOtherJournalsToday) {
                // Check other activity types
                val hasWorkouts = false // Replace with actual check from WorkoutRepository
                val hasRuns = false // Replace with actual check from RunRepository
                val hasMeals = false // Replace with actual check from MealRepository
                val hasPrayers = false // Replace with actual check from PrayerRepository

                if (!hasWorkouts && !hasRuns && !hasMeals && !hasPrayers) {
                    // No activities today, reset streak
                    streakRepository.resetStreak(userId)
                }
            }
        }
    }

    /**
     * Get journal prompts based on user preferences.
     */
    suspend fun getJournalPrompts(
        userId: String,
        mood: String? = null,
        tags: List<String>? = null
    ): List<String> {
        // This would typically call the DeepseekService to get AI-generated journal prompts
        // For now, return a list of default prompts
        return listOf(
            "What are you grateful for today?",
            "What challenges did you face today and how did you overcome them?",
            "What are your goals for tomorrow?",
            "Reflect on a moment that made you smile today.",
            "What did you learn today?"
        )
    }

    /**
     * Search journal entries by content.
     */
    fun searchJournals(userId: String, query: String): Flow<List<Journal>> {
        return journalDao.searchJournals(userId, query)
    }

    /**
     * Sync journals with Supabase.
     */
    suspend fun syncWithRemote(userId: String) {
        try {
            // Get journals from Supabase
            val remoteJournals = supabaseService.getJournalsForUser(userId)
            
            // Get local journals
            val localJournals = journalDao.getAllJournalsForUserAsList(userId)
            
            // Find journals that are in remote but not in local
            val journalsToAdd = remoteJournals.filter { remoteJournal ->
                localJournals.none { it.id == remoteJournal.id }
            }
            
            // Find journals that are in local but not in remote
            val journalsToUpload = localJournals.filter { localJournal ->
                remoteJournals.none { it.id == localJournal.id }
            }
            
            // Find journals that are in both but might have different data
            val journalsToUpdate = localJournals.filter { localJournal ->
                remoteJournals.any { it.id == localJournal.id && it.updatedAt != localJournal.updatedAt }
            }
            
            // Add remote journals to local
            if (journalsToAdd.isNotEmpty()) {
                journalDao.insertJournals(journalsToAdd)
            }
            
            // Upload local journals to remote
            journalsToUpload.forEach { journal ->
                supabaseService.createJournal(journal)
            }
            
            // Update journals that are different
            journalsToUpdate.forEach { localJournal ->
                val remoteJournal = remoteJournals.first { it.id == localJournal.id }
                
                // Use the most recent version
                if (localJournal.updatedAt.isAfter(remoteJournal.updatedAt)) {
                    supabaseService.updateJournal(localJournal)
                } else {
                    journalDao.updateJournal(remoteJournal)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Generate a unique ID for a new journal entry.
     */
    private fun generateJournalId(): String {
        return "journal_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}