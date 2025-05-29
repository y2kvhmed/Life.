package com.life.app.util

import com.life.app.data.local.JournalDao
import com.life.app.data.local.MealDao
import com.life.app.data.local.PrayerDao
import com.life.app.data.local.RunDao
import com.life.app.data.local.WorkoutDao
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ActivityChecker that checks for activities across different repositories.
 */
@Singleton
class ActivityCheckerImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val runDao: RunDao,
    private val mealDao: MealDao,
    private val journalDao: JournalDao,
    private val prayerDao: PrayerDao
) : ActivityChecker {

    /**
     * Checks if the user has any activities (workouts, runs, meals, prayers, journals, etc.)
     * for the specified date.
     *
     * @param userId The ID of the user to check activities for
     * @param date The date to check for activities
     * @return true if the user has any activities on the specified date, false otherwise
     */
    override suspend fun hasActivitiesForDate(userId: String, date: LocalDate): Boolean {
        val startOfDay = LocalDateTime.of(date, LocalTime.MIN)
        val endOfDay = LocalDateTime.of(date, LocalTime.MAX)
        
        // Check for workouts
        val hasWorkouts = workoutDao.getWorkoutsForDateRange(userId, startOfDay, endOfDay).isNotEmpty()
        if (hasWorkouts) return true
        
        // Check for runs
        val hasRuns = runDao.getRunsForDateRange(userId, startOfDay, endOfDay).isNotEmpty()
        if (hasRuns) return true
        
        // Check for meals
        val hasMeals = mealDao.getMealsForDateRange(userId, startOfDay, endOfDay).isNotEmpty()
        if (hasMeals) return true
        
        // Check for journals
        val hasJournals = journalDao.getJournalsForDateRange(userId, startOfDay, endOfDay).isNotEmpty()
        if (hasJournals) return true
        
        // Check for prayers
        val hasPrayers = prayerDao.getPrayersForDateRange(userId, startOfDay, endOfDay).isNotEmpty()
        if (hasPrayers) return true
        
        // No activities found
        return false
    }
}