package com.life.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.model.Religion
import com.life.app.data.repository.BackupRepository
import com.life.app.data.repository.UserRepository
import com.life.app.util.LocaleUtils
import com.life.app.util.ThemeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserSettings()
    }

    /**
     * Load user settings from the repository.
     */
    private fun loadUserSettings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Load user data
                val user = userRepository.getUser()
                
                if (user != null) {
                    // Load theme preferences
                    val theme = ThemeUtils.getCurrentTheme()
                    val accentColor = ThemeUtils.getCurrentAccentColor()
                    
                    // Load language preference
                    val language = LocaleUtils.getCurrentLanguage()
                    
                    // Load notification preferences
                    val notificationsEnabled = userRepository.getNotificationsEnabled()
                    val remindersEnabled = userRepository.getRemindersEnabled()
                    
                    // Load privacy preferences
                    val encryptJournal = userRepository.getEncryptJournal()
                    
                    _uiState.update { it.copy(
                        userEmail = user.email,
                        currentTheme = theme,
                        currentAccentColor = accentColor,
                        currentLanguage = language,
                        currentReligion = user.religion,
                        notificationsEnabled = notificationsEnabled,
                        remindersEnabled = remindersEnabled,
                        encryptJournal = encryptJournal,
                        isLoading = false,
                        errorMessage = ""
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load settings"
                ) }
            }
        }
    }

    /**
     * Set the app theme.
     */
    fun setTheme(theme: String) {
        viewModelScope.launch {
            try {
                ThemeUtils.setCurrentTheme(theme)
                
                _uiState.update { it.copy(
                    currentTheme = theme,
                    showThemeDialog = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to set theme"
                ) }
            }
        }
    }

    /**
     * Set the app accent color.
     */
    fun setAccentColor(accentColor: String) {
        viewModelScope.launch {
            try {
                ThemeUtils.setCurrentAccentColor(accentColor)
                
                _uiState.update { it.copy(
                    currentAccentColor = accentColor,
                    showAccentColorDialog = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to set accent color"
                ) }
            }
        }
    }

    /**
     * Set the app language.
     */
    fun setLanguage(language: String) {
        viewModelScope.launch {
            try {
                LocaleUtils.setCurrentLanguage(language)
                
                _uiState.update { it.copy(
                    currentLanguage = language,
                    showLanguageDialog = false,
                    errorMessage = ""
                ) }
                
                // Save language preference to Supabase
                userRepository.updateLanguage(language)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to set language"
                ) }
            }
        }
    }

    /**
     * Set the user's religion.
     */
    fun setReligion(religion: Religion) {
        viewModelScope.launch {
            try {
                userRepository.updateReligion(religion)
                
                _uiState.update { it.copy(
                    currentReligion = religion,
                    showReligionDialog = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to set religion"
                ) }
            }
        }
    }

    /**
     * Toggle notifications.
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userRepository.setNotificationsEnabled(enabled)
                
                // If notifications are disabled, also disable reminders
                val remindersEnabled = if (enabled) uiState.value.remindersEnabled else false
                if (!enabled) {
                    userRepository.setRemindersEnabled(false)
                }
                
                _uiState.update { it.copy(
                    notificationsEnabled = enabled,
                    remindersEnabled = remindersEnabled,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to update notification settings"
                ) }
            }
        }
    }

    /**
     * Toggle reminders.
     */
    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userRepository.setRemindersEnabled(enabled)
                
                _uiState.update { it.copy(
                    remindersEnabled = enabled,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to update reminder settings"
                ) }
            }
        }
    }

    /**
     * Toggle journal encryption.
     */
    fun toggleJournalEncryption(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userRepository.setEncryptJournal(enabled)
                
                _uiState.update { it.copy(
                    encryptJournal = enabled,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to update encryption settings"
                ) }
            }
        }
    }

    /**
     * Backup user data.
     */
    fun backupData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = "") }
                
                // Create a backup of all user data
                val userId = userRepository.getCurrentUserId()
                if (userId.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "You must be logged in to backup data"
                    ) }
                    return@launch
                }
                
                // Create backup file with timestamp
                val backupData = backupRepository.createBackup(userId)
                val success = backupRepository.saveBackupToStorage(backupData)
                
                if (success) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Backup created successfully"
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save backup file"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to backup data"
                ) }
            }
        }
    }

    /**
     * Sign out the user.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                
                // Navigate to login screen will be handled by the AuthNavigator
                // which observes the authentication state
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to sign out",
                    showSignOutDialog = false
                ) }
            }
        }
    }

    /**
     * Navigate to profile screen.
     */
    fun navigateToProfile() {
        viewModelScope.launch {
            try {
                // Get the current user ID
                val userId = userRepository.getCurrentUserId()
                if (userId.isEmpty()) {
                    _uiState.update { it.copy(
                        errorMessage = "You must be logged in to view your profile"
                    ) }
                    return@launch
                }
                
                // Set the navigation event to profile screen
                // This will be observed by the UI to trigger navigation
                _uiState.update { it.copy(
                    navigationEvent = NavigationEvent.TO_PROFILE
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to navigate to profile"
                ) }
            }
        }
    }

    /**
     * Open privacy policy.
     */
    fun openPrivacyPolicy() {
        // Set the navigation event to privacy policy
        // This will be observed by the UI to trigger navigation or open a browser
        _uiState.update { it.copy(
            navigationEvent = NavigationEvent.TO_PRIVACY_POLICY
        ) }
    }

    /**
     * Open help and support.
     */
    fun openHelpSupport() {
        // Set the navigation event to help and support
        // This will be observed by the UI to trigger navigation or open a browser/email client
        _uiState.update { it.copy(
            navigationEvent = NavigationEvent.TO_HELP_SUPPORT
        ) }
    }

    /**
     * Show the theme dialog.
     */
    fun showThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = true) }
    }

    /**
     * Hide the theme dialog.
     */
    fun hideThemeDialog() {
        _uiState.update { it.copy(showThemeDialog = false) }
    }

    /**
     * Show the accent color dialog.
     */
    fun showAccentColorDialog() {
        _uiState.update { it.copy(showAccentColorDialog = true) }
    }

    /**
     * Hide the accent color dialog.
     */
    fun hideAccentColorDialog() {
        _uiState.update { it.copy(showAccentColorDialog = false) }
    }

    /**
     * Show the language dialog.
     */
    fun showLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    /**
     * Hide the language dialog.
     */
    fun hideLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    /**
     * Show the religion dialog.
     */
    fun showReligionDialog() {
        _uiState.update { it.copy(showReligionDialog = true) }
    }

    /**
     * Hide the religion dialog.
     */
    fun hideReligionDialog() {
        _uiState.update { it.copy(showReligionDialog = false) }
    }

    /**
     * Show the sign out dialog.
     */
    fun showSignOutDialog() {
        _uiState.update { it.copy(showSignOutDialog = true) }
    }

    /**
     * Hide the sign out dialog.
     */
    fun hideSignOutDialog() {
        _uiState.update { it.copy(showSignOutDialog = false) }
    }

    /**
     * Show the about dialog.
     */
    fun showAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    /**
     * Hide the about dialog.
     */
    fun hideAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
    
    /**
     * Clear the success message.
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = "") }
    }
    
    /**
     * Clear the navigation event.
     */
    fun clearNavigationEvent() {
        _uiState.update { it.copy(navigationEvent = NavigationEvent.NONE) }
    }
}

/**
 * Navigation events for the Settings screen.
 */
sealed class NavigationEvent {
    object TO_PROFILE : NavigationEvent()
    object TO_PRIVACY_POLICY : NavigationEvent()
    object TO_HELP_SUPPORT : NavigationEvent()
    object NONE : NavigationEvent()
}

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    val userEmail: String = "",
    val currentTheme: String = ThemeUtils.THEME_SYSTEM,
    val currentAccentColor: String = ThemeUtils.ACCENT_GREEN,
    val currentLanguage: String = LocaleUtils.LANGUAGE_ENGLISH,
    val currentReligion: Religion = Religion.ISLAM,
    val notificationsEnabled: Boolean = true,
    val remindersEnabled: Boolean = true,
    val encryptJournal: Boolean = false,
    val showThemeDialog: Boolean = false,
    val showAccentColorDialog: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showReligionDialog: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val navigationEvent: NavigationEvent = NavigationEvent.NONE
)