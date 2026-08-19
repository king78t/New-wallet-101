package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.FintechGlassCard
import com.example.ui.components.FintechGradientButton
import com.example.ui.components.FintechTextField

@Composable
fun ForgotPasswordScreen(
    viewModel: MainViewModel,
    onBackToLogin: () -> Unit,
    onSendSuccess: () -> Unit
) {
    val email by viewModel.forgotEmail.collectAsState()
    val errorMsg by viewModel.forgotError.collectAsState()
    val isLoading by viewModel.isForgotLoading.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToLogin) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF22C55E)
                )
            }
        }

        FintechGlassCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Forgot Password?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Text(
                text = "Enter your registered Email address to receive a password reset link",
                fontSize = 12.5.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 22.dp)
            )

            FintechTextField(
                value = email,
                onValueChange = { 
                    viewModel.forgotEmail.value = it 
                    if (viewModel.forgotError.value != null) viewModel.forgotError.value = null
                },
                label = "Registered Email Address",
                leadingIcon = Icons.Default.Mail,
                errorMessage = errorMsg,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            FintechGradientButton(
                text = "SEND RESET LINK",
                onClick = {
                    viewModel.sendPasswordReset(onSuccess = onSendSuccess)
                },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Back to Login",
                fontSize = 12.5.sp,
                color = Color(0xFF22C55E),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackToLogin() }
            )
        }
    }
}
