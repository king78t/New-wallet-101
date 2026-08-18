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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.FintechGlassCard
import com.example.ui.components.FintechGradientButton

@Composable
fun VerifyEmailScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    val otpDigits by viewModel.otpDigits.collectAsState()
    val errorMsg by viewModel.otpError.collectAsState()
    val isLoading by viewModel.isOtpLoading.collectAsState()
    val timerSeconds by viewModel.otpCountdown.collectAsState()
    val targetEmail by viewModel.otpEmail.collectAsState()

    val focusRequesters = remember { List(6) { FocusRequester() } }

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
            IconButton(onClick = onBack) {
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
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Verify Email Address",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Text(
                text = if (targetEmail.isNotBlank()) "Enter 6-digit OTP code sent to\n$targetEmail" else "Enter 6-digit OTP code sent to your email",
                fontSize = 12.5.sp,
                color = Color(0xFF4B5563),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 22.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0 until 6) {
                    OutlinedTextField(
                        value = otpDigits.getOrElse(i) { "" },
                        onValueChange = { valText: String ->
                            viewModel.updateOtpDigit(i, valText)
                            if (valText.isNotEmpty() && i < 5) {
                                focusRequesters[i + 1].requestFocus()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .focusRequester(focusRequesters[i]),
                        singleLine = true,
                        isError = errorMsg != null,
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFFFFFF),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFF22C55E),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            errorBorderColor = Color(0xFFEF4444)
                        )
                    )
                }
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (timerSeconds > 0) "Resend Code in 00:${if (timerSeconds < 10) "0" else ""}$timerSeconds" else "Resend Code",
                fontSize = 13.sp,
                color = if (timerSeconds > 0) Color(0xFF64748B) else Color(0xFF22C55E),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(enabled = timerSeconds == 0) {
                    viewModel.resendOtp()
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            FintechGradientButton(
                text = "VERIFY CODE",
                onClick = {
                    viewModel.verifyOtp(onSuccess = onVerifySuccess)
                },
                isLoading = isLoading
            )
        }
    }
}
