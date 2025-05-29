package com.life.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for the Life app.
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class MainApplication : Application()