package com.life.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Enum representing the different types of religions supported in the app.
 */
enum class ReligionType {
    ISLAM,
    CHRISTIANITY,
    JUDAISM
}

/**
 * Enum representing the different types of themes supported in the app.
 */
enum class ThemeType {
    LIGHT,
    DARK,
    AMOLED,
    PASTEL,
    CUSTOM
}

/**
 * Enum representing the different types of workouts supported in the app.
 */
enum class WorkoutType {
    STRENGTH,
    CARDIO,
    FLEXIBILITY,
    CUSTOM
}

/**
 * Enum representing the different types of meals supported in the app.
 */
enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}

/**
 * Entity representing a user in the app.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val religion: ReligionType? = null,
    val theme: ThemeType = ThemeType.LIGHT
)

/**
 * Entity representing a workout in the app.
 */
@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Workout(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val type: WorkoutType,
    val name: String,
    val duration: Int, // in minutes
    val notes: String? = null,
    val difficulty: Int // 1-5 scale
)

/**
 * Entity representing a run in the app.
 */
@Entity(
    tableName = "runs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Run(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val distance: Float, // in kilometers
    val duration: Int, // in minutes
    val pace: Float, // in minutes per kilometer
    val notes: String? = null
)

/**
 * Entity representing a meal in the app.
 */
@Entity(
    tableName = "meals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Meal(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val type: MealType,
    val name: String,
    val calories: Int? = null,
    val notes: String? = null
)

/**
 * Entity representing water intake in the app.
 */
@Entity(
    tableName = "water_intake",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WaterIntake(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val amount: Int // in milliliters
)

/**
 * Entity representing a journal entry in the app.
 */
@Entity(
    tableName = "journals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Journal(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val content: String,
    val mood: String? = null,
    val tags: List<String>? = null,
    val isEncrypted: Boolean = false
)

/**
 * Entity representing a motivation entry in the app.
 */
@Entity(
    tableName = "motivations",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Motivation(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val content: String
)

/**
 * Entity representing a prayer entry in the app.
 */
@Entity(
    tableName = "prayers",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Prayer(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val religion: ReligionType,
    val prayerType: String,
    val completed: Boolean = false
)

/**
 * Entity representing a streak in the app.
 */
@Entity(
    tableName = "streaks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Streak(
    @PrimaryKey val id: String,
    val userId: String,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val count: Int = 0
)

/**
 * Entity representing a note in the app.
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Note(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val content: String,
    val tags: List<String>? = null
)

/**
 * Entity representing a plan in the app.
 */
@Entity(
    tableName = "plans",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Plan(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val title: String,
    val description: String,
    val dueDate: LocalDateTime? = null,
    val completed: Boolean = false
)

/**
 * Entity representing a share in the app.
 */
@Entity(
    tableName = "shares",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Share(
    @PrimaryKey val id: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val content: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)

/**
 * Entity representing a comment in the app.
 */
@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Share::class,
            parentColumns = ["id"],
            childColumns = ["shareId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Comment(
    @PrimaryKey val id: String,
    val userId: String,
    val shareId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val content: String
)