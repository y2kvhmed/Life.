package com.life.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.life.app.data.local.converters.DateTimeConverter
import com.life.app.data.local.converters.LocationPointsConverter
import java.time.LocalDateTime

/**
 * Data model representing a running activity.
 * This entity is stored in the Room database and contains all information about a run.
 */
@Entity(tableName = "runs")
@TypeConverters(DateTimeConverter::class, LocationPointsConverter::class)
data class Run(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * The ID of the user who performed this run.
     * This is used to associate runs with specific users in the database.
     */
    val userId: String,
    
    /**
     * The date and time when the run started.
     */
    val date: LocalDateTime,
    
    /**
     * The total distance covered during the run, in meters.
     */
    val distanceInMeters: Float,
    
    /**
     * The duration of the run, in milliseconds.
     * This excludes any paused time during the run.
     */
    val durationInMillis: Long,
    
    /**
     * The average speed during the run, in kilometers per hour.
     */
    val avgSpeedInKMH: Float,
    
    /**
     * The estimated calories burned during the run.
     */
    val caloriesBurned: Int,
    
    /**
     * A list of location points recorded during the run.
     * Each point is stored as a string in the format "latitude,longitude".
     */
    val locationPoints: List<String> = emptyList(),
    
    /**
     * Optional notes about the run.
     */
    val notes: String = "",
    
    /**
     * Flag indicating whether this run was manually entered rather than GPS-tracked.
     */
    val isManualEntry: Boolean = false
)