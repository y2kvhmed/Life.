package com.life.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for the life. app.
 * Initializes Hilt for dependency injection.
 */
@HiltAndroidApp
class LifeApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide configurations here
    }
}