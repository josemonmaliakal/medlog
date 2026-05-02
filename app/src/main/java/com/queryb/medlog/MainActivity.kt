package com.queryb.medlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.queryb.medlog.auth.AuthManager
import com.queryb.medlog.data.OnboardingPrefs
import com.queryb.medlog.navigation.NavGraph
import com.queryb.medlog.ui.theme.MedLogTheme
import com.queryb.medlog.ui.viewmodel.LabViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authManager     = AuthManager(this)
        val onboardingPrefs = OnboardingPrefs(this)

        setContent {
            MedLogTheme {
                val navController = rememberNavController()
                val viewModel: LabViewModel = viewModel()

                NavGraph(
                    navController   = navController,
                    authManager     = authManager,
                    onboardingPrefs = onboardingPrefs,
                    viewModel       = viewModel
                )
            }
        }
    }
}