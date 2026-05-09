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
    const val LOGIN          = "login"
    const val ONBOARDING     = "onboarding"
    const val HOME           = "home"
    const val ADD            = "add_entry"
    const val PROFILE        = "profile"
    const val DETAIL         = "detail/{metric}"
    const val HISTORY        = "history"
    const val FORGOT         = "forgot_password"

    fun detail(metric: String) = "detail/$metric"
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

    NavHost(
        navController  = navController,
        startDestination = Routes.LOGIN
    ) {

        // ── Login ─────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                authManager      = authManager,
                onLoginSuccess   = {
                    val username = authManager.getUsername()
                    viewModel.setUserId(username)
                    val dest = if (!onboardingPrefs.isCompleteFor(username))
                        Routes.ONBOARDING
                    else
                        Routes.HOME
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPassword = { navController.navigate(Routes.FORGOT) }
            )
        }

        // ── Forgot password ───────────────────────────────────────────────────
        composable(Routes.FORGOT) {
            ForgotPasswordScreen(
                authManager = authManager,
                onBack      = { navController.popBackStack() },
                onSuccess   = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.FORGOT) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                username        = authManager.getUsername(),
                onboardingPrefs = onboardingPrefs,
                authManager     = authManager,    // ← ADD
                onFinished      = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ── Home / Dashboard ──────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                viewModel      = viewModel,
                username       = authManager.getDisplayName()
                    .ifEmpty { authManager.getUsername() },
                onAddClick     = { navController.navigate(Routes.ADD) },
                onDetailClick  = { metric -> navController.navigate(Routes.detail(metric)) },
                onProfileClick = { navController.navigate(Routes.PROFILE) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onLogout       = { goLogin() }
            )
        }

        // ── Add entry ─────────────────────────────────────────────────────────
        composable(Routes.ADD) {
            AddEntryScreen(
                viewModel = viewModel,
                onSaved   = { navController.popBackStack() },
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Routes.PROFILE) {
            ProfileScreen(
                authManager = authManager,
                onBack      = { navController.popBackStack() },
                onLogout    = { goLogin() }
            )
        }

        // ── Chart detail (glucose or cholesterol) ─────────────────────────────
        composable(
            route     = Routes.DETAIL,
            arguments = listOf(
                androidx.navigation.navArgument("metric") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStack ->
            val metric = backStack.arguments?.getString("metric") ?: "glucose"
            ChartDetailScreen(
                metric    = metric,
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Full history ──────────────────────────────────────────────────────
        composable(Routes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}