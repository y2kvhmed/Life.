package com.life.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.life.app.data.local.converters.DateTimeConverter
import com.life.app.data.local.converters.LocationPointsConverter
import com.life.app.data.model.*

/**
 * Main database for the Life application.
 * This database contains all the tables for the application's data.
 */
@Database(
    entities = [
        Run::class,
        Streak::class,
        // Add other entities here as they are created
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    DateTimeConverter::class,
    LocationPointsConverter::class
    // Add other type converters here as they are created
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Get the DAO for run operations.
     */
    abstract fun runDao(): RunDao
    
    /**
     * Get the DAO for streak operations.
     */
    abstract fun streakDao(): StreakDao
    
    // Add other DAOs here as they are created
    
    companion object {
        private const val DATABASE_NAME = "life_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Get the singleton instance of the database.
         * 
         * @param context The application context
         * @return The database instance
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // For simplicity; in production, use proper migrations
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}