package com.life.app.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for handling language preferences and translations.
 */
@Singleton
class LocaleUtils @Inject constructor(
    private val context: Context
) {
    companion object {
        private val Context.dataStore by preferencesDataStore("locale_preferences")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        
        // Supported languages
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_ARABIC = "ar"
        
        // Default language
        const val DEFAULT_LANGUAGE = LANGUAGE_ENGLISH
    }

    /**
     * Get the current language preference as a Flow.
     */
    fun getLanguagePreference(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE
    }

    /**
     * Set the language preference.
     */
    suspend fun setLanguagePreference(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
        updateLocale(languageCode)
    }

    /**
     * Update the app's locale based on the language code.
     */
    fun updateLocale(languageCode: String) {
        val locale = when (languageCode) {
            LANGUAGE_ARABIC -> Locale(LANGUAGE_ARABIC)
            else -> Locale(LANGUAGE_ENGLISH)
        }
        
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    /**
     * Apply the saved language preference.
     */
    fun applyStoredLanguagePreference() {
        // This is called at app startup to apply the saved language preference
        val languageCode = context.dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE
        }
        
        // Since we can't use suspend functions in initialization,
        // we'll set the default locale to the system locale initially
        val systemLocale = Resources.getSystem().configuration.locales.get(0)
        Locale.setDefault(systemLocale)
    }

    /**
     * Check if the current language is right-to-left (RTL).
     */
    fun isRtl(): Boolean {
        return Locale.getDefault().language == LANGUAGE_ARABIC
    }

    /**
     * Get a list of supported languages with their display names.
     */
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            Pair(LANGUAGE_ENGLISH, "English"),
            Pair(LANGUAGE_ARABIC, "العربية")
        )
    }
}

/**
 * Composable function to get the current language preference.
 */
@Composable
fun rememberLanguagePreference(localeUtils: LocaleUtils): String {
    val language by localeUtils.getLanguagePreference().collectAsState(initial = LocaleUtils.DEFAULT_LANGUAGE)
    return language
}