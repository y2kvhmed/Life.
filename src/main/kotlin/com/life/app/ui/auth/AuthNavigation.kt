package com.life.app.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.life.app.util.LocaleUtils

/**
 * Authentication routes for navigation.
 */
object AuthRoutes {
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
}

/**
 * Extension function to add authentication navigation graph to NavGraphBuilder.
 */
fun NavGraphBuilder.authNavigation(
    navController: NavHostController,
    onAuthSuccess: () -> Unit,
    localeUtils: LocaleUtils
) {
    navigation(
        startDestination = AuthRoutes.LOGIN,
        route = AuthRoutes.AUTH_GRAPH
    ) {
        composable(AuthRoutes.LOGIN) {
            val viewModel = hiltViewModel<AuthViewModel>()
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(AuthRoutes.SIGN_UP) },
                onNavigateToForgotPassword = { navController.navigate(AuthRoutes.FORGOT_PASSWORD) },
                onLoginSuccess = onAuthSuccess,
                viewModel = viewModel,
                localeUtils = localeUtils
            )
        }
        
        composable(AuthRoutes.SIGN_UP) {
            val viewModel = hiltViewModel<AuthViewModel>()
            SignUpScreen(
                onNavigateToLogin = { navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(AuthRoutes.AUTH_GRAPH) {
                        saveState = true
                    }
                } },
                onSignUpSuccess = { navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(AuthRoutes.AUTH_GRAPH) {
                        saveState = true
                    }
                } },
                viewModel = viewModel,
                localeUtils = localeUtils
            )
        }
        
        composable(AuthRoutes.FORGOT_PASSWORD) {
            val viewModel = hiltViewModel<AuthViewModel>()
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}

/**
 * Extension function to navigate to the authentication graph.
 */
fun NavHostController.navigateToAuth() {
    this.navigate(AuthRoutes.AUTH_GRAPH) {
        popUpTo(this@navigateToAuth.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}