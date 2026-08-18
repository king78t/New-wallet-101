package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.FintechGlassCard
import com.example.ui.components.FintechGradientButton
import com.example.ui.components.FintechLogoHeader
import com.example.ui.components.FintechSecondaryButton
import com.example.ui.components.FintechSegmentedControl
import com.example.ui.components.FintechTextField
import com.example.ui.validation.ValidationUtils

@Composable
fun UserLoginScreen(
    viewModel: MainViewModel,
    onSwitchToAdmin: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val email by viewModel.loginEmail.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val errorMsg by viewModel.loginError.collectAsState()
    val isLoading by viewModel.isLoginLoading.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        FintechSegmentedControl(
            selectedTab = 0,
            onTabSelected = { tab ->
                if (tab == 1) onSwitchToAdmin()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FintechGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            FintechLogoHeader(
                title = "BP WALLET",
                subtitle = "OFFICIAL WALLET & DEPOSIT SERVICE"
            )

            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )

            Text(
                text = "Please login to continue",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            val isEmailFormatInvalid = email.isNotBlank() && !ValidationUtils.isValidEmailOrPhone(email)
            val isEmailValid = email.isNotBlank() && ValidationUtils.isValidEmailOrPhone(email)
            val emailSuccessText = if (isEmailValid) {
                if (ValidationUtils.isValidEmail(email)) "✓ Valid Email" else "✓ Valid Mobile Number"
            } else null

            FintechTextField(
                value = email,
                onValueChange = { 
                    viewModel.loginEmail.value = it 
                    if (viewModel.loginError.value != null) viewModel.loginError.value = null
                },
                label = "Mobile Number or Email",
                leadingIcon = Icons.Outlined.Person,
                isError = isEmailFormatInvalid || (errorMsg != null && (email.isBlank() || errorMsg?.contains("email", ignoreCase = true) == true)),
                errorMessage = if (isEmailFormatInvalid) "Enter a valid email (e.g. name@domain.com) or phone" else null,
                successMessage = emailSuccessText,
                isLoading = isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(14.dp))

            val isPassLengthInvalid = password.isNotBlank() && password.length < 6
            val isPassValid = password.length >= 6

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Password",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                FintechTextField(
                    value = password,
                    onValueChange = { 
                        viewModel.loginPassword.value = it 
                        if (viewModel.loginError.value != null) viewModel.loginError.value = null
                    },
                    label = "••••••••",
                    leadingIcon = Icons.Outlined.Lock,
                    isError = isPassLengthInvalid || (errorMsg != null && (password.isBlank() || errorMsg?.contains("password", ignoreCase = true) == true || errorMsg?.contains("credential", ignoreCase = true) == true || errorMsg?.contains("failed", ignoreCase = true) == true)),
                    errorMessage = if (isPassLengthInvalid) "Password must be at least 6 characters" else errorMsg,
                    successMessage = if (isPassValid && errorMsg == null) "✓ Valid password format" else null,
                    isLoading = isLoading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF059669),
                            uncheckedColor = Color(0xFF9CA3AF)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Remember Me",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )
                }

                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    color = Color(0xFF059669),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onGoToForgotPassword() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            FintechGradientButton(
                text = "Login",
                onClick = {
                    viewModel.performUserLogin(onSuccess = onLoginSuccess)
                },
                isLoading = isLoading,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.width(150.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFE5E7EB))
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFFE5E7EB))
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            FintechSecondaryButton(
                text = "CREATE NEW ACCOUNT",
                onClick = onGoToRegister
            )
        }
    }
}

