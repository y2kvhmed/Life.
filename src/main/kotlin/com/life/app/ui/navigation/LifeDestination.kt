package com.life.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Fitness
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector
import com.life.app.R

/**
 * Sealed class representing the different destinations in the app.
 */
sealed class LifeDestination(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object Home : LifeDestination("home", R.string.nav_home, Icons.Filled.Home)
    object Coach : LifeDestination("coach", R.string.nav_coach, Icons.Filled.Psychology)
    object Workout : LifeDestination("workout", R.string.nav_workout, Icons.Filled.Fitness)
    object Running : LifeDestination("running", R.string.nav_running, Icons.Filled.DirectionsRun)
    object Nutrition : LifeDestination("nutrition", R.string.nav_nutrition, Icons.Filled.RestaurantMenu)
    object Journal : LifeDestination("journal", R.string.nav_journal, Icons.Filled.Book)
    object Faith : LifeDestination("faith", R.string.nav_faith, Icons.Filled.SelfImprovement)
    object Shares : LifeDestination("shares", R.string.nav_shares, Icons.Filled.Share)
    object Settings : LifeDestination("settings", R.string.nav_settings, Icons.Filled.Settings)
}