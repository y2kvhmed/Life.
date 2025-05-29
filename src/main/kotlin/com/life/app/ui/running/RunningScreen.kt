package com.life.app.ui.running

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.life.app.R
import com.life.app.ui.theme.LifeTheme
import com.life.app.util.DateTimeUtils
import com.life.app.util.PermissionUtils

@Composable
fun RunningScreen(
    viewModel: RunningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val locationPermissionState = PermissionUtils.rememberLocationPermissionState()
    
    LifeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = stringResource(R.string.running),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tabs for History and New Run
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf(
                    stringResource(R.string.history),
                    stringResource(R.string.new_run)
                )
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content based on selected tab
                when (selectedTabIndex) {
                    0 -> RunHistoryTab(
                        runs = uiState.runs,
                        isLoading = uiState.isLoading,
                        onRunClick = { viewModel.selectRun(it) }
                    )
                    1 -> NewRunTab(
                        isTracking = uiState.isTracking,
                        currentDistance = uiState.currentDistance,
                        currentDuration = uiState.currentDuration,
                        hasLocationPermission = locationPermissionState.hasPermission,
                        onRequestLocationPermission = { locationPermissionState.launchPermissionRequest() },
                        onStartRun = { useGps -> viewModel.startRun(useGps) },
                        onStopRun = { viewModel.stopRun() },
                        onSaveRun = { distance, duration, notes -> 
                            viewModel.saveRun(distance, duration, notes) 
                        }
                    )
                }
            }
            
            // Run Detail Dialog
            if (uiState.selectedRun != null) {
                RunDetailDialog(
                    run = uiState.selectedRun!!,
                    onDismiss = { viewModel.deselectRun() },
                    onDelete = { viewModel.deleteRun(uiState.selectedRun!!) }
                )
            }
            
            // Error message
            if (uiState.errorMessage.isNotEmpty()) {
                ErrorSnackbar(
                    message = uiState.errorMessage,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun RunHistoryTab(
    runs: List<RunUiModel>,
    isLoading: Boolean,
    onRunClick: (RunUiModel) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (runs.isEmpty()) {
        EmptyRunsView()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(runs) { run ->
                RunItem(
                    run = run,
                    onClick = { onRunClick(run) }
                )
            }
        }
    }
}

@Composable
fun NewRunTab(
    isTracking: Boolean,
    currentDistance: Float,
    currentDuration: Long,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onStartRun: (Boolean) -> Unit,
    onStopRun: () -> Unit,
    onSaveRun: (Float, Long, String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var manualDistance by remember { mutableStateOf("") }
    var manualDuration by remember { mutableStateOf("") }
    var useGps by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // GPS/Manual toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = useGps,
                onClick = { useGps = true },
                label = { Text(stringResource(R.string.gps_tracking)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FilterChip(
                selected = !useGps,
                onClick = { useGps = false },
                label = { Text(stringResource(R.string.manual_entry)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
        
        if (useGps) {
            // GPS tracking UI
            if (!hasLocationPermission) {
                LocationPermissionRequest(onRequestPermission = onRequestLocationPermission)
            } else {
                GpsTrackingUI(
                    isTracking = isTracking,
                    currentDistance = currentDistance,
                    currentDuration = currentDuration,
                    notes = notes,
                    onNotesChange = { notes = it },
                    onStartRun = { onStartRun(true) },
                    onStopRun = onStopRun,
                    onSaveRun = { onSaveRun(currentDistance, currentDuration, notes) }
                )
            }
        } else {
            // Manual entry UI
            ManualEntryUI(
                distance = manualDistance,
                duration = manualDuration,
                notes = notes,
                onDistanceChange = { manualDistance = it },
                onDurationChange = { manualDuration = it },
                onNotesChange = { notes = it },
                onSaveRun = {
                    val distanceValue = manualDistance.toFloatOrNull() ?: 0f
                    val durationMinutes = manualDuration.toIntOrNull() ?: 0
                    val durationMillis = durationMinutes * 60 * 1000L
                    
                    if (distanceValue > 0 && durationMillis > 0) {
                        onSaveRun(distanceValue, durationMillis, notes)
                        manualDistance = ""
                        manualDuration = ""
                        notes = ""
                    }
                }
            )
        }
    }
}

@Composable
fun LocationPermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.location_permission_needed),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.location_permission_explanation),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

@Composable
fun GpsTrackingUI(
    isTracking: Boolean,
    currentDistance: Float,
    currentDuration: Long,
    notes: String,
    onNotesChange: (String) -> Unit,
    onStartRun: () -> Unit,
    onStopRun: () -> Unit,
    onSaveRun: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current stats
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.distance),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = String.format("%.2f km", currentDistance),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.duration),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = formatDuration(currentDuration),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Start/Stop button
        if (isTracking) {
            Button(
                onClick = onStopRun,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.stop_run))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Button(
                onClick = onStartRun,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.start_run))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Notes field
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.notes)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Save button (only visible when not tracking and has distance)
        if (!isTracking && currentDistance > 0) {
            Button(
                onClick = onSaveRun,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.save_run))
            }
        }
    }
}

@Composable
fun ManualEntryUI(
    distance: String,
    duration: String,
    notes: String,
    onDistanceChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveRun: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = distance,
            onValueChange = { value ->
                // Only allow numbers and decimal point
                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onDistanceChange(value)
                }
            },
            label = { Text(stringResource(R.string.distance_km)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = duration,
            onValueChange = { value ->
                // Only allow numbers
                if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
                    onDurationChange(value)
                }
            },
            label = { Text(stringResource(R.string.duration_minutes)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.notes)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onSaveRun,
            enabled = distance.isNotEmpty() && duration.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.save_run))
        }
    }
}

@Composable
fun EmptyRunsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsRun,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.no_runs_yet),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.track_your_first_run),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun RunItem(
    run: RunUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Run details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = DateTimeUtils.formatDate(run.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = String.format("%.2f km • %s", run.distance, formatDuration(run.duration)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun RunDetailDialog(
    run: RunUiModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.run_details)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Date
                DetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = stringResource(R.string.date),
                    value = DateTimeUtils.formatDate(run.date)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Distance
                DetailRow(
                    icon = Icons.Default.Straighten,
                    label = stringResource(R.string.distance),
                    value = String.format("%.2f km", run.distance)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Duration
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(R.string.duration),
                    value = formatDuration(run.duration)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pace
                DetailRow(
                    icon = Icons.Default.Speed,
                    label = stringResource(R.string.pace),
                    value = calculatePace(run.distance, run.duration)
                )
                
                // Notes (if any)
                if (run.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(R.string.notes),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = run.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.close))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDelete()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.delete))
            }
        }
    )
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(16.dp),
        action = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dismiss))
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.dismiss)
                )
            }
        }
    ) {
        Text(message)
    }
}

// Helper functions
private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun calculatePace(distanceKm: Float, durationMillis: Long): String {
    if (distanceKm <= 0) return "--:--"
    
    val totalMinutes = durationMillis / (1000 * 60)
    val paceMinutes = totalMinutes / distanceKm
    val paceMinutesInt = paceMinutes.toInt()
    val paceSeconds = ((paceMinutes - paceMinutesInt) * 60).toInt()
    
    return String.format("%d:%02d /km", paceMinutesInt, paceSeconds)
}

// Data class for UI
data class RunUiModel(
    val id: Long,
    val date: LocalDateTime,
    val distance: Float,
    val duration: Long,
    val notes: String,
    val mapData: String? = null
)