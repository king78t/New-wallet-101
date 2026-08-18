package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.components.FintechBackground

enum class AuthMode {
    USER_LOGIN,
    CREATE_ACCOUNT,
    ADMIN_LOGIN,
    OTP_VERIFICATION,
    FORGOT_PASSWORD,
    RESET_PASSWORD
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit,
    onAdminLoginSuccess: () -> Unit
) {
    var authMode by remember { mutableStateOf(AuthMode.USER_LOGIN) }
    val isOtpForSignup by viewModel.isOtpForSignup.collectAsState()

    FintechBackground(
        isAdminMode = (authMode == AuthMode.ADMIN_LOGIN)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = authMode,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "auth_screen_transition"
            ) { mode ->
                when (mode) {
                    AuthMode.USER_LOGIN -> UserLoginScreen(
                        viewModel = viewModel,
                        onSwitchToAdmin = { authMode = AuthMode.ADMIN_LOGIN },
                        onGoToRegister = { authMode = AuthMode.CREATE_ACCOUNT },
                        onGoToForgotPassword = { authMode = AuthMode.FORGOT_PASSWORD },
                        onLoginSuccess = onLoginSuccess
                    )

                    AuthMode.ADMIN_LOGIN -> AdminLoginScreen(
                        viewModel = viewModel,
                        onSwitchToUser = { authMode = AuthMode.USER_LOGIN },
                        onAdminLoginSuccess = onAdminLoginSuccess
                    )

                    AuthMode.CREATE_ACCOUNT -> CreateAccountScreen(
                        viewModel = viewModel,
                        onBackToLogin = { authMode = AuthMode.USER_LOGIN },
                        onProceedToOtp = { authMode = AuthMode.OTP_VERIFICATION }
                    )

                    AuthMode.OTP_VERIFICATION -> VerifyEmailScreen(
                        viewModel = viewModel,
                        onBack = { authMode = if (isOtpForSignup) AuthMode.CREATE_ACCOUNT else AuthMode.FORGOT_PASSWORD },
                        onVerifySuccess = {
                            if (isOtpForSignup) {
                                onLoginSuccess()
                            } else {
                                authMode = AuthMode.RESET_PASSWORD
                            }
                        }
                    )

                    AuthMode.FORGOT_PASSWORD -> ForgotPasswordScreen(
                        viewModel = viewModel,
                        onBackToLogin = { authMode = AuthMode.USER_LOGIN },
                        onProceedToOtp = { authMode = AuthMode.OTP_VERIFICATION }
                    )

                    AuthMode.RESET_PASSWORD -> ResetPasswordScreen(
                        viewModel = viewModel,
                        onBackToLogin = { authMode = AuthMode.USER_LOGIN },
                        onResetSuccess = { authMode = AuthMode.USER_LOGIN }
                    )
                }
            }
        }
    }
}
