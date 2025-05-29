package com.life.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.life.app.MainActivity
import com.life.app.R
import com.life.app.data.model.Run
import com.life.app.data.repository.RunRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Service for tracking runs using GPS.
 * This service runs in the foreground and provides location updates for the running feature.
 */
@AndroidEntryPoint
class RunTrackingService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "run_tracking_channel"
        private const val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
        private const val FASTEST_LOCATION_UPDATE_INTERVAL = 2000L // 2 seconds
        
        // Action constants for controlling the service
        const val ACTION_START = "com.life.app.service.ACTION_START"
        const val ACTION_PAUSE = "com.life.app.service.ACTION_PAUSE"
        const val ACTION_RESUME = "com.life.app.service.ACTION_RESUME"
        const val ACTION_STOP = "com.life.app.service.ACTION_STOP"
        
        // Broadcast actions for communicating with the UI
        const val ACTION_LOCATION_UPDATE = "com.life.app.service.ACTION_LOCATION_UPDATE"
        const val ACTION_RUN_STATUS_UPDATE = "com.life.app.service.ACTION_RUN_STATUS_UPDATE"
        
        // Extra keys for intent data
        const val EXTRA_LOCATION = "com.life.app.service.EXTRA_LOCATION"
        const val EXTRA_DISTANCE = "com.life.app.service.EXTRA_DISTANCE"
        const val EXTRA_DURATION = "com.life.app.service.EXTRA_DURATION"
        const val EXTRA_STATUS = "com.life.app.service.EXTRA_STATUS"
    }

    @Inject
    lateinit var runRepository: RunRepository
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    
    private var isTracking = false
    private var isPaused = false
    private var startTime: LocalDateTime? = null
    private var pauseTime: LocalDateTime? = null
    private var totalPausedTime: Long = 0 // in milliseconds
    
    private val locationList = mutableListOf<Location>()
    private var totalDistance = 0f // in meters
    private var lastLocation: Location? = null

    override fun onCreate() {
        super.onCreate()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                
                if (isTracking && !isPaused) {
                    locationResult.locations.forEach { location ->
                        addLocationToList(location)
                        broadcastLocationUpdate(location)
                    }
                }
            }
        }
        
        // Create notification channel for Android O and above
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START -> start()
                ACTION_PAUSE -> pause()
                ACTION_RESUME -> resume()
                ACTION_STOP -> stop()
            }
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopLocationUpdates()
    }

    /**
     * Start tracking a new run.
     */
    private fun start() {
        isTracking = true
        isPaused = false
        startTime = LocalDateTime.now()
        totalPausedTime = 0
        locationList.clear()
        totalDistance = 0f
        lastLocation = null
        
        startForegroundService()
        startLocationUpdates()
        broadcastRunStatusUpdate("STARTED")
    }

    /**
     * Pause the current run tracking.
     */
    private fun pause() {
        isPaused = true
        pauseTime = LocalDateTime.now()
        stopLocationUpdates()
        updateNotification("Run paused")
        broadcastRunStatusUpdate("PAUSED")
    }

    /**
     * Resume the current run tracking after a pause.
     */
    private fun resume() {
        isPaused = false
        pauseTime?.let {
            val pauseDuration = java.time.Duration.between(it, LocalDateTime.now()).toMillis()
            totalPausedTime += pauseDuration
        }
        pauseTime = null
        
        startLocationUpdates()
        updateNotification("Run in progress")
        broadcastRunStatusUpdate("RESUMED")
    }

    /**
     * Stop the current run tracking and save the run data.
     */
    private fun stop() {
        isTracking = false
        isPaused = false
        stopLocationUpdates()
        
        // Save the run data if there are locations recorded
        if (locationList.isNotEmpty() && startTime != null) {
            saveRun()
        }
        
        broadcastRunStatusUpdate("STOPPED")
        stopForeground(true)
        stopSelf()
    }

    /**
     * Start receiving location updates.
     */
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
            .setMinUpdateIntervalMillis(FASTEST_LOCATION_UPDATE_INTERVAL)
            .build()
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Handle permission issues
            e.printStackTrace()
        }
    }

    /**
     * Stop receiving location updates.
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Add a new location to the list and update the total distance.
     */
    private fun addLocationToList(location: Location) {
        locationList.add(location)
        
        // Calculate distance if we have a previous location
        lastLocation?.let {
            val distance = it.distanceTo(location)
            totalDistance += distance
        }
        
        lastLocation = location
    }

    /**
     * Save the run data to the repository.
     */
    private fun saveRun() {
        serviceScope.launch {
            val endTime = LocalDateTime.now()
            val durationInMillis = java.time.Duration.between(startTime, endTime).toMillis() - totalPausedTime
            
            // Create a run object
            val run = Run(
                id = 0, // Will be auto-generated by Room
                userId = "", // Will be set by the repository
                date = startTime ?: LocalDateTime.now(),
                distanceInMeters = totalDistance,
                durationInMillis = durationInMillis,
                avgSpeedInKMH = calculateAverageSpeed(totalDistance, durationInMillis),
                caloriesBurned = calculateCaloriesBurned(totalDistance),
                locationPoints = locationList.map { "${it.latitude},${it.longitude}" }
            )
            
            // Save the run to the repository
            runRepository.addRun(run)
        }
    }

    /**
     * Calculate the average speed in kilometers per hour.
     */
    private fun calculateAverageSpeed(distanceInMeters: Float, durationInMillis: Long): Float {
        if (durationInMillis <= 0) return 0f
        
        val hours = durationInMillis / 1000f / 60f / 60f
        val kilometers = distanceInMeters / 1000f
        
        return kilometers / hours
    }

    /**
     * Calculate the calories burned based on distance.
     * This is a simplified calculation and could be improved with more factors.
     */
    private fun calculateCaloriesBurned(distanceInMeters: Float): Int {
        // A very simple calculation: approximately 60 calories per kilometer
        return (distanceInMeters / 1000f * 60).toInt()
    }

    /**
     * Start the service as a foreground service with a notification.
     */
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Run Tracking")
            .setContentText("Run in progress")
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(pendingIntent)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * Update the notification with a new message.
     */
    private fun updateNotification(message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Run Tracking")
            .setContentText(message)
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Create a notification channel for Android O and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Run Tracking Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Broadcast a location update to the UI.
     */
    private fun broadcastLocationUpdate(location: Location) {
        val intent = Intent(ACTION_LOCATION_UPDATE)
        intent.putExtra(EXTRA_LOCATION, location)
        intent.putExtra(EXTRA_DISTANCE, totalDistance)
        intent.putExtra(EXTRA_DURATION, calculateDuration())
        sendBroadcast(intent)
    }

    /**
     * Broadcast a run status update to the UI.
     */
    private fun broadcastRunStatusUpdate(status: String) {
        val intent = Intent(ACTION_RUN_STATUS_UPDATE)
        intent.putExtra(EXTRA_STATUS, status)
        intent.putExtra(EXTRA_DISTANCE, totalDistance)
        intent.putExtra(EXTRA_DURATION, calculateDuration())
        sendBroadcast(intent)
    }

    /**
     * Calculate the current duration of the run, accounting for paused time.
     */
    private fun calculateDuration(): Long {
        if (startTime == null) return 0
        
        val currentTime = LocalDateTime.now()
        var duration = java.time.Duration.between(startTime, currentTime).toMillis()
        
        // Subtract paused time
        duration -= totalPausedTime
        
        // If currently paused, also subtract the current pause duration
        if (isPaused && pauseTime != null) {
            val currentPauseDuration = java.time.Duration.between(pauseTime, currentTime).toMillis()
            duration -= currentPauseDuration
        }
        
        return duration
    }
}