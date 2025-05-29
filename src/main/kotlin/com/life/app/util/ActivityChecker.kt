package com.life.app.util

import java.time.LocalDate

/**
 * Interface for checking if a user has any activities on a specific date.
 * This is used to avoid circular dependencies between repositories
 * when checking for streak maintenance.
 */
interface ActivityChecker {
    /**
     * Checks if the user has any activities (workouts, runs, meals, prayers, journals, etc.)
     * for the specified date.
     *
     * @param userId The ID of the user to check activities for
     * @param date The date to check for activities
     * @return true if the user has any activities on the specified date, false otherwise
     */
    suspend fun hasActivitiesForDate(userId: String, date: LocalDate): Boolean
}