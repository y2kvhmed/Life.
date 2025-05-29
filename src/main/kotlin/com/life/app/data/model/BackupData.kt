package com.life.app.data.model

import java.time.LocalDateTime

/**
 * Data class representing a backup of user data.
 */
data class BackupData(
    val userId: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val workouts: List<Workout> = emptyList(),
    val runs: List<Run> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val journals: List<Journal> = emptyList(),
    val prayers: List<Prayer> = emptyList(),
    val notes: List<Note> = emptyList(),
    val motivations: List<Motivation> = emptyList(),
    val plans: List<Plan> = emptyList(),
    val shares: List<Share> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val streaks: List<Streak> = emptyList(),
    val userSettings: UserSettings? = null
)