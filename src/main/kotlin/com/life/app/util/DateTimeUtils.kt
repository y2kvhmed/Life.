package com.life.app.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Utility class for date and time operations.
 */
object DateTimeUtils {

    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
    
    /**
     * Format a LocalDateTime to a time string (e.g., "3:30 PM").
     */
    fun formatTime(dateTime: LocalDateTime): String {
        return dateTime.format(timeFormatter)
    }
    
    /**
     * Format a LocalDateTime to a date string (e.g., "Jan 1, 2023").
     */
    fun formatDate(dateTime: LocalDateTime): String {
        return dateTime.format(dateFormatter)
    }
    
    /**
     * Format a LocalDateTime to a date and time string (e.g., "Jan 1, 2023 at 3:30 PM").
     */
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
    
    /**
     * Get a relative time string (e.g., "Just now", "5 minutes ago", "Yesterday", etc.).
     */
    fun getRelativeTimeSpan(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(dateTime, now)
        val hours = ChronoUnit.HOURS.between(dateTime, now)
        val days = ChronoUnit.DAYS.between(dateTime, now)
        
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            days < 2 -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> formatDate(dateTime)
        }
    }
    
    /**
     * Check if a LocalDateTime is today.
     */
    fun isToday(dateTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        return dateTime.toLocalDate() == now.toLocalDate()
    }
    
    /**
     * Get the start of the day for a given LocalDateTime.
     */
    fun getStartOfDay(dateTime: LocalDateTime): LocalDateTime {
        return dateTime.toLocalDate().atStartOfDay()
    }
    
    /**
     * Get the end of the day for a given LocalDateTime.
     */
    fun getEndOfDay(dateTime: LocalDateTime): LocalDateTime {
        return dateTime.toLocalDate().atTime(23, 59, 59)
    }
    
    /**
     * Get the start of the current day.
     */
    fun getStartOfToday(): LocalDateTime {
        return getStartOfDay(LocalDateTime.now())
    }
    
    /**
     * Get the end of the current day.
     */
    fun getEndOfToday(): LocalDateTime {
        return getEndOfDay(LocalDateTime.now())
    }
}