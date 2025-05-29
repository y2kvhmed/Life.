package com.life.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Apple-inspired Light Theme Colors
private val LightColors = lightColorScheme(
    primary = Color(0xFF007AFF),       // iOS Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1F0FF),
    onPrimaryContainer = Color(0xFF00325C),
    secondary = Color(0xFF5E5CE6),     // iOS Purple
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9E9FF),
    onSecondaryContainer = Color(0xFF1D1D4E),
    tertiary = Color(0xFFFF9500),      // iOS Orange
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEFD5),
    onTertiaryContainer = Color(0xFF3A2800),
    error = Color(0xFFFF3B30),         // iOS Red
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF2F2F7),    // iOS Light Gray Background
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA), // iOS Light Gray
    onSurfaceVariant = Color(0xFF3A3A3C),
    outline = Color(0xFFC6C6C8)         // iOS Separator
)

// Apple-inspired Dark Theme Colors
private val DarkColors = darkColorScheme(
    primary = Color(0xFF0A84FF),       // iOS Blue (Dark)
    onPrimary = Color.White,
    primaryContainer = Color(0xFF153057),
    onPrimaryContainer = Color(0xFFADD8FF),
    secondary = Color(0xFF5E5CE6),     // iOS Purple (Dark)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2C2C54),
    onSecondaryContainer = Color(0xFFD0D0FF),
    tertiary = Color(0xFFFF9F0A),      // iOS Orange (Dark)
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF553A00),
    onTertiaryContainer = Color(0xFFFFE0B0),
    error = Color(0xFFFF453A),         // iOS Red (Dark)
    errorContainer = Color(0xFF5C0000),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1C1E),    // iOS Dark Gray Background
    onBackground = Color(0xFFE5E5EA),
    surface = Color(0xFF2C2C2E),       // iOS Dark Gray Surface
    onSurface = Color(0xFFE5E5EA),
    surfaceVariant = Color(0xFF3A3A3C), // iOS Dark Gray
    onSurfaceVariant = Color(0xFFD1D1D6),
    outline = Color(0xFF636366)         // iOS Separator (Dark)
)

// Apple-inspired AMOLED Theme (True Black)
private val AmoledColors = darkColorScheme(
    primary = Color(0xFF0A84FF),       // iOS Blue (Dark)
    onPrimary = Color.White,
    primaryContainer = Color(0xFF153057),
    onPrimaryContainer = Color(0xFFADD8FF),
    secondary = Color(0xFF5E5CE6),     // iOS Purple (Dark)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2C2C54),
    onSecondaryContainer = Color(0xFFD0D0FF),
    tertiary = Color(0xFFFF9F0A),      // iOS Orange (Dark)
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF553A00),
    onTertiaryContainer = Color(0xFFFFE0B0),
    error = Color(0xFFFF453A),         // iOS Red (Dark)
    errorContainer = Color(0xFF5C0000),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black,          // True Black
    onBackground = Color(0xFFE5E5EA),
    surface = Color.Black,             // True Black
    onSurface = Color(0xFFE5E5EA),
    surfaceVariant = Color(0xFF1C1C1E), // Slightly lighter than black
    onSurfaceVariant = Color(0xFFD1D1D6),
    outline = Color(0xFF636366)         // iOS Separator (Dark)
)

// Apple-inspired Pastel Theme
private val PastelColors = lightColorScheme(
    primary = Color(0xFF98C1FF),       // Pastel Blue
    onPrimary = Color(0xFF002A77),
    primaryContainer = Color(0xFFE0ECFF),
    onPrimaryContainer = Color(0xFF001947),
    secondary = Color(0xFFBFBEFF),     // Pastel Purple
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFFE9E8FF),
    onSecondaryContainer = Color(0xFF121C2B),
    tertiary = Color(0xFFFFD0A1),      // Pastel Orange
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = Color(0xFFFFEFD5),
    onTertiaryContainer = Color(0xFF1D1433),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F7FC),    // Soft Background
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFBFF),       // Soft Surface
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFEFEDF7), // Soft Surface Variant
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFCBC4D6)         // Soft Outline
)

/**
 * The theme for the life. app.
 * Supports Light, Dark, AMOLED, and Pastel themes.
 */
@Composable
fun LifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeType: ThemeType = ThemeType.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeType == ThemeType.LIGHT -> LightColors
        themeType == ThemeType.DARK -> DarkColors
        themeType == ThemeType.AMOLED -> AmoledColors
        themeType == ThemeType.PASTEL -> PastelColors
        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Enum representing the available theme types in the app.
 */
enum class ThemeType {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED,
    PASTEL,
    CUSTOM
}