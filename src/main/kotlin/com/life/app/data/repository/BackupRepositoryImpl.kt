package com.life.app.data.repository

import android.content.Context
import android.os.Environment
import com.google.gson.GsonBuilder
import com.life.app.data.local.CommentDao
import com.life.app.data.local.JournalDao
import com.life.app.data.local.MealDao
import com.life.app.data.local.MotivationDao
import com.life.app.data.local.NoteDao
import com.life.app.data.local.PlanDao
import com.life.app.data.local.PrayerDao
import com.life.app.data.local.RunDao
import com.life.app.data.local.ShareDao
import com.life.app.data.local.StreakDao
import com.life.app.data.local.WorkoutDao
import com.life.app.data.model.BackupData
import com.life.app.util.LocalDateTimeAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the BackupRepository interface.
 */
@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutDao: WorkoutDao,
    private val runDao: RunDao,
    private val mealDao: MealDao,
    private val journalDao: JournalDao,
    private val prayerDao: PrayerDao,
    private val noteDao: NoteDao,
    private val motivationDao: MotivationDao,
    private val planDao: PlanDao,
    private val shareDao: ShareDao,
    private val commentDao: CommentDao,
    private val streakDao: StreakDao,
    private val userRepository: UserRepository
) : BackupRepository {

    /**
     * Creates a backup of all user data.
     *
     * @param userId The ID of the user whose data to backup
     * @return A BackupData object containing all the user's data
     */
    override suspend fun createBackup(userId: String): BackupData {
        // Get all user data from the database
        val workouts = workoutDao.getAllWorkoutsForUser(userId)
        val runs = runDao.getAllRunsForUser(userId)
        val meals = mealDao.getAllMealsForUser(userId)
        val journals = journalDao.getAllJournalsForUser(userId)
        val prayers = prayerDao.getAllPrayersForUser(userId)
        val notes = noteDao.getAllNotesForUser(userId)
        val motivations = motivationDao.getAllMotivationsForUser(userId)
        val plans = planDao.getAllPlansForUser(userId)
        val shares = shareDao.getAllSharesForUser(userId)
        val comments = commentDao.getAllCommentsForUser(userId)
        val streaks = streakDao.getAllStreaksForUser(userId)
        val userSettings = userRepository.getUserSettings(userId)

        // Create and return the backup data
        return BackupData(
            userId = userId,
            timestamp = LocalDateTime.now(),
            workouts = workouts,
            runs = runs,
            meals = meals,
            journals = journals,
            prayers = prayers,
            notes = notes,
            motivations = motivations,
            plans = plans,
            shares = shares,
            comments = comments,
            streaks = streaks,
            userSettings = userSettings
        )
    }

    /**
     * Saves a backup to external storage.
     *
     * @param backupData The backup data to save
     * @return true if the backup was saved successfully, false otherwise
     */
    override suspend fun saveBackupToStorage(backupData: BackupData): Boolean {
        try {
            // Create a directory for backups if it doesn't exist
            val backupDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "LifeAppBackups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Create a file for the backup with a timestamp
            val timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(backupData.timestamp)
            val backupFile = File(backupDir, "life_backup_${timestamp}.json")

            // Create a Gson instance with a LocalDateTime adapter
            val gson = GsonBuilder()
                .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create()

            // Write the backup data to the file
            FileWriter(backupFile).use { writer ->
                gson.toJson(backupData, writer)
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Restores a backup from external storage.
     *
     * @param backupData The backup data to restore
     * @return true if the backup was restored successfully, false otherwise
     */
    override suspend fun restoreBackup(backupData: BackupData): Boolean {
        try {
            // Clear existing data for the user
            val userId = backupData.userId
            
            // Insert all data from the backup
            backupData.workouts.forEach { workoutDao.insertWorkout(it) }
            backupData.runs.forEach { runDao.insertRun(it) }
            backupData.meals.forEach { mealDao.insertMeal(it) }
            backupData.journals.forEach { journalDao.insertJournal(it) }
            backupData.prayers.forEach { prayerDao.insertPrayer(it) }
            backupData.notes.forEach { noteDao.insertNote(it) }
            backupData.motivations.forEach { motivationDao.insertMotivation(it) }
            backupData.plans.forEach { planDao.insertPlan(it) }
            backupData.shares.forEach { shareDao.insertShare(it) }
            backupData.comments.forEach { commentDao.insertComment(it) }
            backupData.streaks.forEach { streakDao.insertStreak(it) }
            
            // Restore user settings if available
            backupData.userSettings?.let { userRepository.updateUserSettings(it) }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}