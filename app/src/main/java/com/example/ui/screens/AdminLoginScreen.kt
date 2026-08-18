package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.FintechGlassCard
import com.example.ui.components.FintechGradientButton
import com.example.ui.components.FintechLogoHeader
import com.example.ui.components.FintechSegmentedControl
import com.example.ui.components.FintechTextField

@Composable
fun AdminLoginScreen(
    viewModel: MainViewModel,
    onSwitchToUser: () -> Unit,
    onAdminLoginSuccess: () -> Unit
) {
    val email by viewModel.adminEmail.collectAsState()
    val password by viewModel.adminPassword.collectAsState()
    val errorMsg by viewModel.adminError.collectAsState()
    val isLoading by viewModel.isAdminLoading.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        FintechSegmentedControl(
            selectedTab = 1,
            onTabSelected = { tab ->
                if (tab == 0) onSwitchToUser()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        FintechGlassCard(
            modifier = Modifier.fillMaxWidth(),
            isAdminMode = true
        ) {
            FintechLogoHeader(
                title = "BP WALLET",
                subtitle = "SECURE ADMIN PORTAL",
                isAdminMode = true
            )

            Text(
                text = "Welcome Back Admin",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD97706)
            )

            Text(
                text = "Super Admin Role Authentication",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
            )

            FintechTextField(
                value = email,
                onValueChange = { 
                    viewModel.adminEmail.value = it 
                    if (viewModel.adminError.value != null) viewModel.adminError.value = null
                },
                label = "Super Admin Username",
                leadingIcon = Icons.Default.Person,
                isError = errorMsg != null && (email.isBlank() || errorMsg?.contains("username", ignoreCase = true) == true || errorMsg?.contains("invalid", ignoreCase = true) == true),
                isLoading = isLoading,
                isAdminMode = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            FintechTextField(
                value = password,
                onValueChange = { 
                    viewModel.adminPassword.value = it 
                    if (viewModel.adminError.value != null) viewModel.adminError.value = null
                },
                label = "Admin Password",
                leadingIcon = Icons.Default.VpnKey,
                isError = errorMsg != null && (password.isBlank() || errorMsg?.contains("password", ignoreCase = true) == true || errorMsg?.contains("invalid", ignoreCase = true) == true || errorMsg?.contains("failed", ignoreCase = true) == true),
                errorMessage = if (errorMsg != null) errorMsg else null,
                isLoading = isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFFD97706)
                        )
                    }
                },
                isAdminMode = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            FintechGradientButton(
                text = "Login",
                onClick = {
                    viewModel.performAdminLogin(onSuccess = onAdminLoginSuccess)
                },
                isLoading = isLoading,
                isAdminMode = true,
                icon = Icons.Default.Shield
            )
        }
    }
}
