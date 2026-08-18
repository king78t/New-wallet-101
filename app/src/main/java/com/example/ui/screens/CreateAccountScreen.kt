package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.draw.shadow
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
import com.example.ui.components.FintechSegmentedControl
import com.example.ui.components.FintechTextField

@Composable
fun CreateAccountScreen(
    viewModel: MainViewModel,
    onBackToLogin: () -> Unit,
    onProceedToOtp: () -> Unit
) {
    val fullName by viewModel.regFullName.collectAsState()
    val email by viewModel.regEmail.collectAsState()
    val phone by viewModel.regPhone.collectAsState()
    val password by viewModel.regPassword.collectAsState()
    val confirmPassword by viewModel.regConfirmPassword.collectAsState()
    val selectedCountry by viewModel.regCountry.collectAsState()
    val selectedCurrency by viewModel.regCurrency.collectAsState()
    val errorMsg by viewModel.regError.collectAsState()
    val isLoading by viewModel.isRegLoading.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        FintechSegmentedControl(
            selectedTab = 0,
            onTabSelected = { tab ->
                if (tab == 1) onBackToLogin()
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
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )

            Text(
                text = "Join BP Wallet for instant 24/7 payouts",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Full Name Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Full Name",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                FintechTextField(
                    value = fullName,
                    onValueChange = { 
                        viewModel.regFullName.value = it 
                        if (viewModel.regError.value != null) viewModel.regError.value = null
                    },
                    label = "e.g. Hamza Malik",
                    leadingIcon = Icons.Outlined.Person,
                    isLoading = isLoading
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email Address Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Email Address",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                FintechTextField(
                    value = email,
                    onValueChange = { 
                        viewModel.regEmail.value = it 
                        if (viewModel.regError.value != null) viewModel.regError.value = null
                    },
                    label = "hamza@bpexch.com",
                    leadingIcon = Icons.Outlined.Email,
                    isLoading = isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Select Country & Currency Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Select Country & Currency",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val countries = listOf(
                    Triple("Pakistan (+92)", "🇵🇰", "PKR"),
                    Triple("UAE (+971)", "🇦🇪", "AED"),
                    Triple("Saudi Arabia (+966)", "🇸🇦", "SAR")
                )

                countries.forEach { (cName, flag, curr) ->
                    val isSelected = selectedCountry.contains(cName.take(5), ignoreCase = true) || selectedCurrency == curr || (cName.contains("Saudi") && selectedCountry.contains("Saudi"))
                    val dialCode = if (curr == "PKR") "+92" else if (curr == "AED") "+971" else "+966"

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF059669) else Color(0xFFF9FAFB))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF059669) else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.onCountrySelected(cName) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = flag,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = curr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF111827)
                            )
                            Text(
                                text = dialCode,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // WhatsApp / Mobile Number Header
            val activeCountryLabel = if (selectedCurrency == "PKR") "🇵🇰 Pakistan (+92)"
                else if (selectedCurrency == "AED") "🇦🇪 UAE (+971)"
                else "🇸🇦 Saudi Arabia (+966)"

            val activeDialCode = if (selectedCurrency == "PKR") "+92"
                else if (selectedCurrency == "AED") "+971"
                else "+966"

            val activeFlag = if (selectedCurrency == "PKR") "🇵🇰"
                else if (selectedCurrency == "AED") "🇦🇪"
                else "🇸🇦"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WhatsApp / Mobile Number",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Text(
                    text = activeCountryLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mobile Input Field Box with Country Code Prefix Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFAFAFA))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Flag + Code Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE5E7EB))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$activeFlag  $activeDialCode",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    BasicTextField(
                        value = phone,
                        onValueChange = { 
                            viewModel.regPhone.value = it 
                            if (viewModel.regError.value != null) viewModel.regError.value = null
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (phone.isEmpty()) {
                                Text(
                                    text = "501234567",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Text(
                text = "Locked to ${if (selectedCurrency == "PKR") "Pakistan" else if (selectedCurrency == "AED") "UAE" else "Saudi Arabia"}. Enter local number (e.g. 501234567).",
                fontSize = 10.5.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
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
                        viewModel.regPassword.value = it 
                        if (viewModel.regError.value != null) viewModel.regError.value = null
                    },
                    label = "••••••••",
                    leadingIcon = Icons.Outlined.Lock,
                    isLoading = isLoading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Confirm Password Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Confirm Password",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                FintechTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        viewModel.regConfirmPassword.value = it 
                        if (viewModel.regError.value != null) viewModel.regError.value = null
                    },
                    label = "••••••••",
                    leadingIcon = Icons.Outlined.Key,
                    errorMessage = errorMsg,
                    isLoading = isLoading,
                    visualTransformation = PasswordVisualTransformation()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            FintechGradientButton(
                text = "REGISTER ACCOUNT",
                onClick = {
                    viewModel.performUserSignup(onNavigateToVerifyOtp = onProceedToOtp)
                },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Already registered? Login here",
                fontSize = 12.5.sp,
                color = Color(0xFF059669),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onBackToLogin() }
            )
        }
    }
}

