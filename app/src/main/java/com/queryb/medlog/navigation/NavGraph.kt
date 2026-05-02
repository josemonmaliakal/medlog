package com.queryb.medlog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.queryb.medlog.auth.AuthManager
import com.queryb.medlog.data.OnboardingPrefs
import com.queryb.medlog.ui.screens.*
import com.queryb.medlog.ui.viewmodel.LabViewModel

object Routes {
    const val LOGIN      = "login"
    const val ONBOARDING = "onboarding"
    const val HOME       = "home"
    const val ADD        = "add_entry"
    const val CHART      = "chart"
    const val PROFILE    = "profile"
    const val DETAIL     = "detail/{metric}"   // ← ADD
    fun detail(metric: String) = "detail/$metric"  // ← helper
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authManager: AuthManager,
    onboardingPrefs: OnboardingPrefs,
    viewModel: LabViewModel
) {
    fun goLogin() {
        authManager.logout()
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                authManager = authManager,
                onLoginSuccess = {
                    val username = authManager.getUsername()
                    viewModel.setUserId(username)

                    // Check onboarding per THIS specific user
                    val dest = if (!onboardingPrefs.isCompleteFor(username))
                        Routes.ONBOARDING
                    else
                        Routes.HOME

                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                username        = authManager.getUsername(),   // ← pass username
                onboardingPrefs = onboardingPrefs,             // ← pass prefs
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel     = viewModel,
                username      = authManager.getDisplayName().ifEmpty { authManager.getUsername() },
                onAddClick    = { navController.navigate(Routes.ADD) },
                onDetailClick = { metric -> navController.navigate(Routes.detail(metric)) }, // ← updated
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onLogout      = { goLogin() }
            )
        }

        composable(Routes.ADD) {
            AddEntryScreen(
                viewModel = viewModel,
                onSaved   = { navController.popBackStack() },
                onBack    = { navController.popBackStack() }
            )
        }

        composable(Routes.CHART) {
            ChartScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                authManager = authManager,
                onBack      = { navController.popBackStack() },
                onLogout    = { goLogin() }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                androidx.navigation.navArgument("metric") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStack ->
            val metric = backStack.arguments?.getString("metric") ?: "glucose"
            ChartDetailScreen(
                metric    = metric,
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}