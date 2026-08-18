package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.FintechGlassCard
import com.example.ui.components.FintechGradientButton
import com.example.ui.components.FintechTextField

@Composable
fun ResetPasswordScreen(
    viewModel: MainViewModel,
    onBackToLogin: () -> Unit,
    onResetSuccess: () -> Unit
) {
    val newPassword by viewModel.resetNewPassword.collectAsState()
    val confirmPassword by viewModel.resetConfirmPassword.collectAsState()
    val errorMsg by viewModel.resetError.collectAsState()
    val isLoading by viewModel.isResetLoading.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        FintechGlassCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Reset Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Text(
                text = "Create a new strong password for your BP Wallet account",
                fontSize = 12.5.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 22.dp)
            )

            FintechTextField(
                value = newPassword,
                onValueChange = { 
                    viewModel.resetNewPassword.value = it 
                    if (viewModel.resetError.value != null) viewModel.resetError.value = null
                },
                label = "New Password",
                leadingIcon = Icons.Default.Lock,
                isLoading = isLoading,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(12.dp))

            FintechTextField(
                value = confirmPassword,
                onValueChange = { 
                    viewModel.resetConfirmPassword.value = it 
                    if (viewModel.resetError.value != null) viewModel.resetError.value = null
                },
                label = "Confirm New Password",
                leadingIcon = Icons.Default.Key,
                errorMessage = errorMsg,
                isLoading = isLoading,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(24.dp))

            FintechGradientButton(
                text = "UPDATE PASSWORD",
                onClick = {
                    viewModel.updatePassword(onSuccess = onResetSuccess)
                },
                isLoading = isLoading
            )
        }
    }
}
