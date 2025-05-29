package com.life.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.life.app.data.local.converters.DateTimeConverter
import java.time.LocalDateTime

/**
 * Data model representing a user's activity streak.
 * This entity is stored in the Room database and contains information about a user's consecutive days of activity.
 */
@Entity(tableName = "streaks")
@TypeConverters(DateTimeConverter::class)
data class Streak(
    @PrimaryKey
    val id: String,
    
    /**
     * The ID of the user who owns this streak.
     */
    val userId: String,
    
    /**
     * The date and time when the streak was last updated.
     * This is used to determine if the streak should be incremented or reset.
     */
    val updatedAt: LocalDateTime,
    
    /**
     * The current streak count, representing the number of consecutive days with activity.
     */
    val count: Int
)