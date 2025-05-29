package com.life.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

/**
 * Utility class for handling permissions in the app.
 */
object PermissionUtils {

    /**
     * Check if the location permission is granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if the notification permission is granted (Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Notification permission is implicitly granted on Android < 13
        }
    }

    /**
     * Composable function to request location permission.
     */
    @Composable
    fun rememberLocationPermissionLauncher(
        onPermissionResult: (Boolean) -> Unit
    ) = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult
    )

    /**
     * Composable function to request notification permission (Android 13+).
     */
    @Composable
    fun rememberNotificationPermissionLauncher(
        onPermissionResult: (Boolean) -> Unit
    ) = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult
    )

    /**
     * Get the list of permissions required by the app.
     */
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        // Add location permission (optional for run tracking)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        
        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return permissions
    }
}