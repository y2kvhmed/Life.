package com.life.app.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.life.app.ui.theme.ThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for managing theme preferences.
 */
@Singleton
class ThemeUtils @Inject constructor(
    private val context: Context
) {
    companion object {
        private val Context.dataStore by preferencesDataStore("theme_preferences")
        private val THEME_KEY = stringPreferencesKey("theme")
        private val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
        
        // Default theme and accent color
        const val DEFAULT_THEME = "LIGHT"
        const val DEFAULT_ACCENT_COLOR = "GREEN"
    }

    /**
     * Get the current theme preference as a Flow.
     */
    fun getThemePreference(): Flow<ThemeType> = context.dataStore.data.map { preferences ->
        val themeString = preferences[THEME_KEY] ?: DEFAULT_THEME
        try {
            ThemeType.valueOf(themeString)
        } catch (e: IllegalArgumentException) {
            ThemeType.LIGHT
        }
    }

    /**
     * Set the theme preference.
     */
    suspend fun setThemePreference(themeType: ThemeType) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = themeType.name
        }
    }

    /**
     * Get the current accent color preference as a Flow.
     */
    fun getAccentColorPreference(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ACCENT_COLOR_KEY] ?: DEFAULT_ACCENT_COLOR
    }

    /**
     * Set the accent color preference.
     */
    suspend fun setAccentColorPreference(accentColor: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCENT_COLOR_KEY] = accentColor
        }
    }

    /**
     * Get a list of available themes.
     */
    fun getAvailableThemes(): List<ThemeType> {
        return ThemeType.values().toList()
    }

    /**
     * Get a list of available accent colors.
     */
    fun getAvailableAccentColors(): List<Pair<String, String>> {
        return listOf(
            Pair("GREEN", "#4CAF50"),
            Pair("BLUE", "#2196F3"),
            Pair("PURPLE", "#9C27B0"),
            Pair("ORANGE", "#FF9800"),
            Pair("RED", "#F44336"),
            Pair("PINK", "#E91E63"),
            Pair("TEAL", "#009688"),
            Pair("INDIGO", "#3F51B5"),
            Pair("AMBER", "#FFC107"),
            Pair("DEEP_PURPLE", "#673AB7"),
            Pair("LIGHT_BLUE", "#03A9F4"),
            Pair("CYAN", "#00BCD4"),
            Pair("LIGHT_GREEN", "#8BC34A"),
            Pair("LIME", "#CDDC39"),
            Pair("DEEP_ORANGE", "#FF5722"),
            Pair("BROWN", "#795548"),
            Pair("GREY", "#9E9E9E"),
            Pair("BLUE_GREY", "#607D8B")
        )
    }
}

/**
 * Composable function to get the current theme preference.
 */
@Composable
fun rememberThemePreference(themeUtils: ThemeUtils): ThemeType {
    val theme by themeUtils.getThemePreference().collectAsState(initial = ThemeType.LIGHT)
    return theme
}

/**
 * Composable function to get the current accent color preference.
 */
@Composable
fun rememberAccentColorPreference(themeUtils: ThemeUtils): String {
    val accentColor by themeUtils.getAccentColorPreference().collectAsState(initial = ThemeUtils.DEFAULT_ACCENT_COLOR)
    return accentColor
}