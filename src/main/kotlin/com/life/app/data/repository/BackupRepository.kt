package com.life.app.data.repository

import com.life.app.data.model.BackupData

/**
 * Repository interface for handling backup operations.
 */
interface BackupRepository {
    /**
     * Creates a backup of all user data.
     *
     * @param userId The ID of the user whose data to backup
     * @return A BackupData object containing all the user's data
     */
    suspend fun createBackup(userId: String): BackupData
    
    /**
     * Saves a backup to external storage.
     *
     * @param backupData The backup data to save
     * @return true if the backup was saved successfully, false otherwise
     */
    suspend fun saveBackupToStorage(backupData: BackupData): Boolean
    
    /**
     * Restores a backup from external storage.
     *
     * @param backupData The backup data to restore
     * @return true if the backup was restored successfully, false otherwise
     */
    suspend fun restoreBackup(backupData: BackupData): Boolean
}