package com.life.app.data.repository

import com.life.app.data.local.PrayerDao
import com.life.app.data.model.Prayer
import com.life.app.data.model.ReligionType
import com.life.app.data.remote.SupabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing prayer data.
 */
@Singleton
class PrayerRepository @Inject constructor(
    private val prayerDao: PrayerDao,
    private val supabaseService: SupabaseService,
    private val streakRepository: StreakRepository,
    private val userRepository: UserRepository
) {

    /**
     * Get all prayers for a user.
     */
    fun getAllPrayers(userId: String): Flow<List<Prayer>> {
        return prayerDao.getAllPrayersForUser(userId)
    }

    /**
     * Get prayers for a specific date range.
     */
    fun getPrayersForDateRange(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Prayer>> {
        return prayerDao.getPrayersForDateRange(userId, startDate, endDate)
    }

    /**
     * Get a specific prayer by ID.
     */
    suspend fun getPrayerById(prayerId: String): Prayer? {
        return prayerDao.getPrayerById(prayerId)
    }

    /**
     * Create a new prayer record.
     */
    suspend fun createPrayer(
        userId: String,
        name: String,
        religionType: ReligionType,
        notes: String? = null
    ): Prayer {
        val prayer = Prayer(
            id = generatePrayerId(),
            userId = userId,
            name = name,
            religionType = religionType,
            notes = notes,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save locally
        prayerDao.insertPrayer(prayer)

        // Save to Supabase
        try {
            supabaseService.createPrayer(prayer)
        } catch (e: Exception) {
            // Handle error, but don't block the local save
        }

        // Update streak
        streakRepository.updateStreakForActivity(userId)

        return prayer
    }

    /**
     * Update an existing prayer record.
     */
    suspend fun updatePrayer(prayer: Prayer): Prayer {
        val updatedPrayer = prayer.copy(updatedAt = LocalDateTime.now())

        // Update locally
        prayerDao.updatePrayer(updatedPrayer)

        // Update in Supabase
        try {
            supabaseService.updatePrayer(updatedPrayer)
        } catch (e: Exception) {
            // Handle error, but don't block the local update
        }

        return updatedPrayer
    }

    /**
     * Delete a prayer record.
     */
    suspend fun deletePrayer(prayer: Prayer) {
        // Delete locally
        prayerDao.deletePrayer(prayer)

        // Delete from Supabase
        try {
            supabaseService.deletePrayer(prayer.id)
        } catch (e: Exception) {
            // Handle error, but don't block the local delete
        }

        // Check if we need to update streak
        val userId = prayer.userId
        val today = LocalDateTime.now()
        val startOfDay = today.withHour(0).withMinute(0).withSecond(0)
        val endOfDay = today.withHour(23).withMinute(59).withSecond(59)

        // If the deleted prayer was from today, check if there are any other activities today
        if (prayer.createdAt.isAfter(startOfDay) && prayer.createdAt.isBefore(endOfDay)) {
            val hasOtherPrayersToday = getPrayersForDateRange(userId, startOfDay, endOfDay).first().isNotEmpty()
            
            if (!hasOtherPrayersToday) {
                // Check other activity types
                val hasWorkouts = false // Replace with actual check from WorkoutRepository
                val hasRuns = false // Replace with actual check from RunRepository
                val hasMeals = false // Replace with actual check from MealRepository
                val hasJournals = false // Replace with actual check from JournalRepository

                if (!hasWorkouts && !hasRuns && !hasMeals && !hasJournals) {
                    // No activities today, reset streak
                    streakRepository.resetStreak(userId)
                }
            }
        }
    }

    /**
     * Get prayer times for a specific religion.
     */
    suspend fun getPrayerTimes(userId: String, date: LocalDateTime): List<String> {
        // Get the user's religion
        val user = userRepository.getUserById(userId)
        val religionType = user?.religionType ?: ReligionType.NONE

        // Return prayer times based on religion
        return when (religionType) {
            ReligionType.ISLAM -> getIslamicPrayerTimes(date)
            ReligionType.CHRISTIANITY -> getChristianPrayerTimes()
            ReligionType.JUDAISM -> getJewishPrayerTimes()
            else -> emptyList()
        }
    }

    /**
     * Get Islamic prayer times for a specific date.
     * In a real app, this would use a prayer time calculation library or API.
     */
    private fun getIslamicPrayerTimes(date: LocalDateTime): List<String> {
        // This is a placeholder. In a real app, you would use a prayer time calculation
        // library or API to get accurate prayer times based on location and date.
        return listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    }

    /**
     * Get Christian prayer times.
     */
    private fun getChristianPrayerTimes(): List<String> {
        return listOf("Morning Prayer", "Midday Prayer", "Evening Prayer")
    }

    /**
     * Get Jewish prayer times.
     */
    private fun getJewishPrayerTimes(): List<String> {
        return listOf("Shacharit", "Mincha", "Maariv")
    }

    /**
     * Get religious messages or verses.
     */
    suspend fun getReligiousMessages(userId: String): List<String> {
        // Get the user's religion
        val user = userRepository.getUserById(userId)
        val religionType = user?.religionType ?: ReligionType.NONE

        // This would typically call the DeepseekService to get AI-generated religious messages
        // For now, return a list of default messages based on religion
        return when (religionType) {
            ReligionType.ISLAM -> listOf(
                "Indeed, with hardship comes ease.",
                "Verily, Allah is with the patient.",
                "And when My servants ask you concerning Me - indeed I am near."
            )
            ReligionType.CHRISTIANITY -> listOf(
                "For God so loved the world that he gave his one and only Son.",
                "I can do all things through Christ who strengthens me.",
                "The Lord is my shepherd; I shall not want."
            )
            ReligionType.JUDAISM -> listOf(
                "Hear, O Israel: The Lord our God, the Lord is one.",
                "Justice, justice shall you pursue.",
                "Who is wise? One who learns from every person."
            )
            else -> emptyList()
        }
    }

    /**
     * Sync prayers with Supabase.
     */
    suspend fun syncWithRemote(userId: String) {
        try {
            // Get prayers from Supabase
            val remotePrayers = supabaseService.getPrayersForUser(userId)
            
            // Get local prayers
            val localPrayers = prayerDao.getAllPrayersForUserAsList(userId)
            
            // Find prayers that are in remote but not in local
            val prayersToAdd = remotePrayers.filter { remotePrayer ->
                localPrayers.none { it.id == remotePrayer.id }
            }
            
            // Find prayers that are in local but not in remote
            val prayersToUpload = localPrayers.filter { localPrayer ->
                remotePrayers.none { it.id == localPrayer.id }
            }
            
            // Find prayers that are in both but might have different data
            val prayersToUpdate = localPrayers.filter { localPrayer ->
                remotePrayers.any { it.id == localPrayer.id && it.updatedAt != localPrayer.updatedAt }
            }
            
            // Add remote prayers to local
            if (prayersToAdd.isNotEmpty()) {
                prayerDao.insertPrayers(prayersToAdd)
            }
            
            // Upload local prayers to remote
            prayersToUpload.forEach { prayer ->
                supabaseService.createPrayer(prayer)
            }
            
            // Update prayers that are different
            prayersToUpdate.forEach { localPrayer ->
                val remotePrayer = remotePrayers.first { it.id == localPrayer.id }
                
                // Use the most recent version
                if (localPrayer.updatedAt.isAfter(remotePrayer.updatedAt)) {
                    supabaseService.updatePrayer(localPrayer)
                } else {
                    prayerDao.updatePrayer(remotePrayer)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Generate a unique ID for a new prayer record.
     */
    private fun generatePrayerId(): String {
        return "prayer_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}