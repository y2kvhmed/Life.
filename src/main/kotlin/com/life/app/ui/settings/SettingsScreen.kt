package com.life.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.life.app.R
import com.life.app.data.model.Religion
import com.life.app.ui.theme.LifeTheme
import com.life.app.util.LocaleUtils
import com.life.app.util.ThemeUtils

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Handle navigation events
    LaunchedEffect(uiState.navigationEvent) {
        when (uiState.navigationEvent) {
            is NavigationEvent.TO_PROFILE -> {
                navController.navigate("profile")
                viewModel.clearNavigationEvent()
            }
            is NavigationEvent.TO_PRIVACY_POLICY -> {
                // Open privacy policy in browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://lifeapp.com/privacy"))
                context.startActivity(intent)
                viewModel.clearNavigationEvent()
            }
            is NavigationEvent.TO_HELP_SUPPORT -> {
                // Open email client to contact support
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@lifeapp.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Life App Support Request")
                }
                context.startActivity(intent)
                viewModel.clearNavigationEvent()
            }
            else -> { /* Do nothing */ }
        }
    }
    
    LifeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                item {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
                
                // Account Section
                item {
                    SettingsSectionHeader(title = stringResource(R.string.account))
                    
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.profile),
                        subtitle = uiState.userEmail,
                        onClick = { viewModel.navigateToProfile() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Logout,
                        title = stringResource(R.string.sign_out),
                        subtitle = stringResource(R.string.sign_out_description),
                        onClick = { viewModel.showSignOutDialog() }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Appearance Section
                item {
                    SettingsSectionHeader(title = stringResource(R.string.appearance))
                    
                    // Theme Selection
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.theme),
                        subtitle = ThemeUtils.getThemeName(uiState.currentTheme),
                        onClick = { viewModel.showThemeDialog() }
                    )
                    
                    // Accent Color Selection
                    SettingsItem(
                        icon = Icons.Default.ColorLens,
                        title = stringResource(R.string.accent_color),
                        subtitle = ThemeUtils.getAccentColorName(uiState.currentAccentColor),
                        onClick = { viewModel.showAccentColorDialog() },
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ThemeUtils.getAccentColorValue(uiState.currentAccentColor))
                            )
                        }
                    )
                    
                    // Language Selection
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.language),
                        subtitle = LocaleUtils.getLanguageName(uiState.currentLanguage),
                        onClick = { viewModel.showLanguageDialog() }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Religion Section
                item {
                    SettingsSectionHeader(title = stringResource(R.string.faith))
                    
                    // Religion Selection
                    SettingsItem(
                        icon = when (uiState.currentReligion) {
                            Religion.ISLAM -> Icons.Default.Crescent
                            Religion.CHRISTIANITY -> Icons.Default.Church
                            Religion.JUDAISM -> Icons.Default.Star
                        },
                        title = stringResource(R.string.religion),
                        subtitle = when (uiState.currentReligion) {
                            Religion.ISLAM -> stringResource(R.string.islam)
                            Religion.CHRISTIANITY -> stringResource(R.string.christianity)
                            Religion.JUDAISM -> stringResource(R.string.judaism)
                        },
                        onClick = { viewModel.showReligionDialog() }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Notifications Section
                item {
                    SettingsSectionHeader(title = stringResource(R.string.notifications))
                    
                    // Notification Settings
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.enable_notifications),
                        subtitle = stringResource(R.string.notifications_description),
                        isChecked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                    
                    // Reminder Settings
                    SettingsToggleItem(
                        icon = Icons.Default.Alarm,
                        title = stringResource(R.string.daily_reminders),
                        subtitle = stringResource(R.string.reminders_description),
                        isChecked = uiState.remindersEnabled,
                        onCheckedChange = { viewModel.toggleReminders(it) },
                        enabled = uiState.notificationsEnabled
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Privacy Section
                item {
                    SettingsSectionHeader(title = stringResource(R.string.privacy))
                    
                    // Data Encryption
                    SettingsToggleItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.encrypt_journal),
                        subtitle = stringResource(R.string.encrypt_journal_description),
                        isChecked = uiState.encryptJournal,
                        onCheckedChange = { viewModel.toggleJournalEncryption(it) }
                    )
                    
                    // Data Backup
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = stringResource(R.string.backup_data),
                        subtitle = stringResource(R.string.backup_description),
                        onClick = { viewModel.backupData() }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // About Section
                item {
                    SettingsSectionHeader(title = stringResource(R.string.about))
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.about_app),
                        subtitle = stringResource(R.string.app_version, "1.0.0"),
                        onClick = { viewModel.showAboutDialog() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Policy,
                        title = stringResource(R.string.privacy_policy),
                        subtitle = stringResource(R.string.privacy_policy_description),
                        onClick = { viewModel.openPrivacyPolicy() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = stringResource(R.string.help_support),
                        subtitle = stringResource(R.string.help_description),
                        onClick = { viewModel.openHelpSupport() }
                    )
                    
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            
            // Theme Dialog
            if (uiState.showThemeDialog) {
                ThemeSelectionDialog(
                    currentTheme = uiState.currentTheme,
                    onThemeSelected = { viewModel.setTheme(it) },
                    onDismiss = { viewModel.hideThemeDialog() }
                )
            }
            
            // Accent Color Dialog
            if (uiState.showAccentColorDialog) {
                AccentColorSelectionDialog(
                    currentAccentColor = uiState.currentAccentColor,
                    onAccentColorSelected = { viewModel.setAccentColor(it) },
                    onDismiss = { viewModel.hideAccentColorDialog() }
                )
            }
            
            // Language Dialog
            if (uiState.showLanguageDialog) {
                LanguageSelectionDialog(
                    currentLanguage = uiState.currentLanguage,
                    onLanguageSelected = { viewModel.setLanguage(it) },
                    onDismiss = { viewModel.hideLanguageDialog() }
                )
            }
            
            // Religion Dialog
            if (uiState.showReligionDialog) {
                ReligionSelectionDialog(
                    currentReligion = uiState.currentReligion,
                    onReligionSelected = { viewModel.setReligion(it) },
                    onDismiss = { viewModel.hideReligionDialog() }
                )
            }
            
            // Sign Out Dialog
            if (uiState.showSignOutDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideSignOutDialog() },
                    title = { Text(stringResource(R.string.sign_out)) },
                    text = { Text(stringResource(R.string.sign_out_confirmation)) },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.signOut() }
                        ) {
                            Text(stringResource(R.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.hideSignOutDialog() }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
            
            // About Dialog
            if (uiState.showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideAboutDialog() },
                    title = { Text(stringResource(R.string.about_app)) },
                    text = {
                        Column {
                            Text(stringResource(R.string.app_name))
                            Text(stringResource(R.string.app_version, "1.0.0"))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.app_description))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.hideAboutDialog() }
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                )
            }
            
            // Error message
            if (uiState.errorMessage.isNotEmpty()) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    action = {
                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text(
                                text = stringResource(R.string.dismiss),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Text(uiState.errorMessage)
                }
            }
            
            // Success message
            if (uiState.successMessage.isNotEmpty()) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    action = {
                        TextButton(
                            onClick = { viewModel.clearSuccessMessage() }
                        ) {
                            Text(
                                text = stringResource(R.string.dismiss),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Text(uiState.successMessage)
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = { onCheckedChange(!isChecked) })
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = { onCheckedChange(it) },
                enabled = enabled
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = ThemeUtils.getAvailableThemes()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_theme)) },
        text = {
            LazyColumn {
                items(themes) { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onThemeSelected(theme) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = ThemeUtils.getThemeName(theme),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun AccentColorSelectionDialog(
    currentAccentColor: String,
    onAccentColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val accentColors = ThemeUtils.getAvailableAccentColors()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_accent_color)) },
        text = {
            LazyColumn {
                items(accentColors) { accentColor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccentColorSelected(accentColor) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = accentColor == currentAccentColor,
                            onClick = { onAccentColorSelected(accentColor) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ThemeUtils.getAccentColorValue(accentColor))
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = ThemeUtils.getAccentColorName(accentColor),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = LocaleUtils.getSupportedLanguages()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            LazyColumn {
                items(languages) { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = { onLanguageSelected(language) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = LocaleUtils.getLanguageName(language),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun ReligionSelectionDialog(
    currentReligion: Religion,
    onReligionSelected: (Religion) -> Unit,
    onDismiss: () -> Unit
) {
    val religions = listOf(Religion.ISLAM, Religion.CHRISTIANITY, Religion.JUDAISM)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_religion)) },
        text = {
            LazyColumn {
                items(religions) { religion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReligionSelected(religion) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = religion == currentReligion,
                            onClick = { onReligionSelected(religion) }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Icon(
                            imageVector = when (religion) {
                                Religion.ISLAM -> Icons.Default.Crescent
                                Religion.CHRISTIANITY -> Icons.Default.Church
                                Religion.JUDAISM -> Icons.Default.Star
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = when (religion) {
                                Religion.ISLAM -> stringResource(R.string.islam)
                                Religion.CHRISTIANITY -> stringResource(R.string.christianity)
                                Religion.JUDAISM -> stringResource(R.string.judaism)
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}