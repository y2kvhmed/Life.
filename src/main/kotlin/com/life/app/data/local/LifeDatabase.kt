package com.life.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.life.app.data.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Type converters for Room database.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun fromReligionType(value: ReligionType): String {
        return value.name
    }

    @TypeConverter
    fun toReligionType(value: String): ReligionType {
        return ReligionType.valueOf(value)
    }

    @TypeConverter
    fun fromThemeType(value: ThemeType): String {
        return value.name
    }

    @TypeConverter
    fun toThemeType(value: String): ThemeType {
        return ThemeType.valueOf(value)
    }

    @TypeConverter
    fun fromWorkoutType(value: WorkoutType): String {
        return value.name
    }

    @TypeConverter
    fun toWorkoutType(value: String): WorkoutType {
        return WorkoutType.valueOf(value)
    }

    @TypeConverter
    fun fromMealType(value: MealType): String {
        return value.name
    }

    @TypeConverter
    fun toMealType(value: String): MealType {
        return MealType.valueOf(value)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }
}

/**
 * Room database for the life. app.
 */
@Database(
    entities = [
        User::class,
        Workout::class,
        Run::class,
        Meal::class,
        WaterIntake::class,
        Journal::class,
        Motivation::class,
        Prayer::class,
        Streak::class,
        Note::class,
        Plan::class,
        Share::class,
        Comment::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LifeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun runDao(): RunDao
    abstract fun mealDao(): MealDao
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun journalDao(): JournalDao
    abstract fun motivationDao(): MotivationDao
    abstract fun prayerDao(): PrayerDao
    abstract fun streakDao(): StreakDao
    abstract fun noteDao(): NoteDao
    abstract fun planDao(): PlanDao
    abstract fun shareDao(): ShareDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var INSTANCE: LifeDatabase? = null

        fun getDatabase(context: Context): LifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeDatabase::class.java,
                    "life_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}