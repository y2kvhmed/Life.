package com.life.app.data.local

import androidx.room.*
import com.life.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for User entity.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

/**
 * Data Access Object for Workout entity.
 */
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWorkoutsByUserId(userId: String): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkoutById(id: String): Flow<Workout?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("SELECT * FROM workouts WHERE userId = :userId AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getWorkoutsByDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Workout>>
}

/**
 * Data Access Object for Run entity.
 */
@Dao
interface RunDao {
    @Query("SELECT * FROM runs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRunsByUserId(userId: String): Flow<List<Run>>

    @Query("SELECT * FROM runs WHERE id = :id")
    fun getRunById(id: String): Flow<Run?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Update
    suspend fun updateRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM runs WHERE userId = :userId AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getRunsByDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Run>>
}

/**
 * Data Access Object for Meal entity.
 */
@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMealsByUserId(userId: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :id")
    fun getMealById(id: String): Flow<Meal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Update
    suspend fun updateMeal(meal: Meal)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("SELECT * FROM meals WHERE userId = :userId AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getMealsByDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND type = :mealType AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getMealsByTypeAndDateRange(userId: String, mealType: MealType, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Meal>>
}

/**
 * Data Access Object for WaterIntake entity.
 */
@Dao
interface WaterIntakeDao {
    @Query("SELECT * FROM water_intake WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWaterIntakeByUserId(userId: String): Flow<List<WaterIntake>>

    @Query("SELECT SUM(amount) FROM water_intake WHERE userId = :userId AND createdAt >= :startDate AND createdAt <= :endDate")
    fun getTotalWaterIntakeForDay(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterIntake(waterIntake: WaterIntake)

    @Update
    suspend fun updateWaterIntake(waterIntake: WaterIntake)

    @Delete
    suspend fun deleteWaterIntake(waterIntake: WaterIntake)
}

/**
 * Data Access Object for Journal entity.
 */
@Dao
interface JournalDao {
    @Query("SELECT * FROM journals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getJournalsByUserId(userId: String): Flow<List<Journal>>

    @Query("SELECT * FROM journals WHERE id = :id")
    fun getJournalById(id: String): Flow<Journal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: Journal)

    @Update
    suspend fun updateJournal(journal: Journal)

    @Delete
    suspend fun deleteJournal(journal: Journal)

    @Query("SELECT * FROM journals WHERE userId = :userId AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getJournalsByDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Journal>>

    @Query("SELECT * FROM journals WHERE userId = :userId AND mood LIKE :mood ORDER BY createdAt DESC")
    fun getJournalsByMood(userId: String, mood: String): Flow<List<Journal>>
}

/**
 * Data Access Object for Motivation entity.
 */
@Dao
interface MotivationDao {
    @Query("SELECT * FROM motivations WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestMotivationByUserId(userId: String): Flow<Motivation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotivation(motivation: Motivation)

    @Update
    suspend fun updateMotivation(motivation: Motivation)

    @Delete
    suspend fun deleteMotivation(motivation: Motivation)
}

/**
 * Data Access Object for Prayer entity.
 */
@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayers WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPrayersByUserId(userId: String): Flow<List<Prayer>>

    @Query("SELECT * FROM prayers WHERE userId = :userId AND religion = :religion AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getPrayersByReligionAndDateRange(userId: String, religion: ReligionType, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Prayer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayer: Prayer)

    @Update
    suspend fun updatePrayer(prayer: Prayer)

    @Delete
    suspend fun deletePrayer(prayer: Prayer)
}

/**
 * Data Access Object for Streak entity.
 */
@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE userId = :userId")
    fun getStreakByUserId(userId: String): Flow<Streak?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: Streak)

    @Update
    suspend fun updateStreak(streak: Streak)

    @Delete
    suspend fun deleteStreak(streak: Streak)
}

/**
 * Data Access Object for Note entity.
 */
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getNotesByUserId(userId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: String): Flow<Note?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}

/**
 * Data Access Object for Plan entity.
 */
@Dao
interface PlanDao {
    @Query("SELECT * FROM plans WHERE userId = :userId ORDER BY dueDate ASC, createdAt DESC")
    fun getPlansByUserId(userId: String): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :id")
    fun getPlanById(id: String): Flow<Plan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: Plan)

    @Update
    suspend fun updatePlan(plan: Plan)

    @Delete
    suspend fun deletePlan(plan: Plan)

    @Query("SELECT * FROM plans WHERE userId = :userId AND completed = :completed ORDER BY dueDate ASC, createdAt DESC")
    fun getPlansByCompletionStatus(userId: String, completed: Boolean): Flow<List<Plan>>
}

/**
 * Data Access Object for Share entity.
 */
@Dao
interface ShareDao {
    @Query("SELECT * FROM shares ORDER BY createdAt DESC")
    fun getAllShares(): Flow<List<Share>>

    @Query("SELECT * FROM shares ORDER BY likesCount DESC")
    fun getAllSharesByLikes(): Flow<List<Share>>

    @Query("SELECT * FROM shares ORDER BY commentsCount DESC")
    fun getAllSharesByComments(): Flow<List<Share>>

    @Query("SELECT * FROM shares WHERE id = :id")
    fun getShareById(id: String): Flow<Share?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShare(share: Share)

    @Update
    suspend fun updateShare(share: Share)

    @Delete
    suspend fun deleteShare(share: Share)
}

/**
 * Data Access Object for Comment entity.
 */
@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE shareId = :shareId ORDER BY createdAt ASC")
    fun getCommentsByShareId(shareId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Update
    suspend fun updateComment(comment: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)
}