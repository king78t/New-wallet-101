package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.AdminSuperPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BetProWebViewScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UserDashboardScreen
import com.example.ui.theme.BPWalletTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BPWalletTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val toastMsg by viewModel.toastMessage.collectAsState()

                    LaunchedEffect(toastMsg) {
                        toastMsg?.let { msg ->
                            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                            viewModel.clearToast()
                        }
                    }

                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("auth") {
            AuthScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("user_dashboard") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onAdminLoginSuccess = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("user_dashboard") {
            UserDashboardScreen(
                viewModel = viewModel,
                onOpenBetProExchange = {
                    navController.navigate("betpro_webview")
                },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("auth") {
                        popUpTo("user_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("betpro_webview") {
            BetProWebViewScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("admin_dashboard") {
            AdminSuperPanelScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate("auth") {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}
