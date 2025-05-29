package com.life.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.repository.UserRepository
import com.life.app.util.LocaleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling authentication-related operations.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localeUtils: LocaleUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Check if the user is already logged in.
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val isLoggedIn = userRepository.isUserLoggedIn()
                _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
            } catch (e: Exception) {
                // Handle error
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to check auth status") }
            }
        }
    }

    /**
     * Login with email and password.
     */
    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) return

        _uiState.update { it.copy(isLoading = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                userRepository.signIn(email, password)
                _uiState.update { it.copy(isLoggedIn = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Login failed. Please check your credentials."
                    ) 
                }
            }
        }
    }

    /**
     * Register a new user.
     */
    fun register(email: String, password: String, confirmPassword: String) {
        if (!validateRegistrationInput(email, password, confirmPassword)) return

        _uiState.update { it.copy(isLoading = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                userRepository.signUp(email, password)
                _uiState.update { it.copy(isRegistrationSuccessful = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Registration failed. Please try again."
                    ) 
                }
            }
        }
    }

    /**
     * Send a password reset email.
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = "") }

        viewModelScope.launch {
            try {
                userRepository.resetPassword(email)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isPasswordResetSent = true,
                        successMessage = "Password reset instructions sent to your email"
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Failed to send reset instructions. Please try again."
                    ) 
                }
            }
        }
    }

    /**
     * Sign out the current user.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                _uiState.update { it.copy(isLoggedIn = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to sign out") }
            }
        }
    }

    /**
     * Set the app language.
     */
    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                localeUtils.setLanguagePreference(languageCode)
                // No need to update UI state as the locale change will trigger a recomposition
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to change language") }
            }
        }
    }

    /**
     * Validate login input fields.
     */
    private fun validateLoginInput(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password cannot be empty") }
            return false
        }
        return true
    }

    /**
     * Validate registration input fields.
     */
    private fun validateRegistrationInput(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required") }
            return false
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return false
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return false
        }

        return true
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }

    /**
     * Reset the registration success state.
     */
    fun resetRegistrationState() {
        _uiState.update { it.copy(isRegistrationSuccessful = false) }
    }

    /**
     * Reset the password reset state.
     */
    fun resetPasswordResetState() {
        _uiState.update { it.copy(isPasswordResetSent = false, successMessage = "") }
    }
}

/**
 * UI state for authentication screens.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isRegistrationSuccessful: Boolean = false,
    val isPasswordResetSent: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)