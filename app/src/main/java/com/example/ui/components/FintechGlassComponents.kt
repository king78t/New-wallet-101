package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BpErrorRed
import com.example.ui.theme.BpGoldPrimary
import com.example.ui.theme.BpGreenEmerald
import com.example.ui.theme.BpGreenPrimary
import kotlin.math.roundToInt

// -----------------------------------------------------------------------------
// 1. LIGHT-THEME FINTECH AMBIENT BACKGROUND WITH FLOATING ORBS & GLASS OVERLAYS
// -----------------------------------------------------------------------------
@Composable
fun FintechBackground(
    isAdminMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val rotationAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotationAnim.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(30000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isAdminMode) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFFFBEB),
                            Color(0xFFF8FAFC)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFECFDF5),
                            Color(0xFFF1F5F9),
                            Color(0xFFF8FAFC)
                        )
                    )
                }
            )
    ) {
        // Floating Soft Mint / Emerald / Gold Blur Orbs
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-40).dp, y = (-20).dp)
                .rotate(rotationAnim.value)
                .blur(80.dp)
                .background(
                    color = if (isAdminMode) Color(0xFFFEF3C7).copy(alpha = 0.70f)
                    else Color(0xFFD1FAE5).copy(alpha = 0.80f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .rotate(-rotationAnim.value)
                .blur(90.dp)
                .background(
                    color = if (isAdminMode) Color(0xFFFDE68A).copy(alpha = 0.50f)
                    else Color(0xFFA7F3D0).copy(alpha = 0.60f),
                    shape = CircleShape
                )
        )

        // Light reflection streak overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.40f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.20f)
                        )
                    )
                )
        )

        content()
    }
}

// -----------------------------------------------------------------------------
// 2. FROSTED WHITE GLASS CARD (24dp Corners, Floating Shadow, Glass Border)
// -----------------------------------------------------------------------------
@Composable
fun FintechGlassCard(
    modifier: Modifier = Modifier,
    isAdminMode: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (isAdminMode) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFFDE68A),
                Color(0xFFF59E0B).copy(alpha = 0.4f),
                Color(0xFFFDE68A)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color.White,
                Color(0xFF22C55E).copy(alpha = 0.35f),
                Color(0xFFE2E8F0)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (isAdminMode) Color(0x20F59E0B) else Color(0x2022C55E),
                spotColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF9FFFFFF), // 98% crisp white glass
                        Color(0xF2FFFFFF)  // 95% crisp white glass
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Glossy Top Reflection Streak
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.60f),
                            Color.White.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

// -----------------------------------------------------------------------------
// 3. BRAND LOGO HEADER
// -----------------------------------------------------------------------------
@Composable
fun FintechLogoHeader(
    title: String = "BP WALLET",
    subtitle: String = "OFFICIAL WALLET & DEPOSIT SERVICE",
    isAdminMode: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = if (isAdminMode) Color(0x30F59E0B) else Color(0x3010B981),
                    spotColor = if (isAdminMode) Color(0x20F59E0B) else Color(0x2010B981)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = if (isAdminMode) Color(0xFFFDE68A) else Color(0xFFA7F3D0),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.example.R.drawable.bp_wallet_icon_1786476389724),
                contentDescription = "BP Wallet Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = if (isAdminMode) Color(0xFFD97706) else Color(0xFF059669)
        )

        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 4. ELEGANT LIGHT INPUT FIELD WITH GREEN/GOLD FOCUS GLOW
// -----------------------------------------------------------------------------
@Composable
fun FintechTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    successMessage: String? = null,
    isLoading: Boolean = false,
    isAdminMode: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    // Shake Animation on Error State
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(isError) {
        if (isError) {
            repeat(4) {
                shakeOffset.animateTo(12f, tween(40))
                shakeOffset.animateTo(-12f, tween(40))
            }
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            label = {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = label,
                    tint = if (isError) BpErrorRed else if (isAdminMode) BpGoldPrimary else Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFFFFF),
                unfocusedContainerColor = Color(0xFFFAFAFA),
                focusedBorderColor = if (isAdminMode) BpGoldPrimary else Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                errorBorderColor = BpErrorRed,
                focusedLabelColor = if (isAdminMode) BpGoldPrimary else Color(0xFF10B981),
                unfocusedLabelColor = Color(0xFF6B7280),
                errorLabelColor = BpErrorRed,
                focusedLeadingIconColor = if (isAdminMode) BpGoldPrimary else Color(0xFF10B981),
                unfocusedLeadingIconColor = Color(0xFF9CA3AF),
                errorLeadingIconColor = BpErrorRed,
                focusedTextColor = Color(0xFF111827),
                unfocusedTextColor = Color(0xFF111827),
                errorTextColor = Color(0xFF111827)
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = BpErrorRed,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        } else if (!successMessage.isNullOrBlank()) {
            Text(
                text = successMessage,
                color = Color(0xFF059669),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        } else if (!helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                color = Color(0xFF6B7280),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 5. PREMIUM FULL-WIDTH OR CAPSULE GRADIENT BUTTON (#10B981 -> #059669)
// -----------------------------------------------------------------------------
@Composable
fun FintechGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isAdminMode: Boolean = false,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = 300f),
        label = "btn_press_scale"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .graphicsLayer(scaleX = scaleAnim, scaleY = scaleAnim)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isAdminMode) Color(0x30F59E0B) else Color(0x3010B981),
                spotColor = if (isAdminMode) Color(0x20D97706) else Color(0x20059669)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = if (isAdminMode) {
                    Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                }
            )
            .clickable(
                enabled = !isLoading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 6. SECONDARY OUTLINE GLASS BUTTON
// -----------------------------------------------------------------------------
@Composable
fun FintechSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .shadow(2.dp, RoundedCornerShape(23.dp), ambientColor = Color(0x10000000))
            .clip(RoundedCornerShape(23.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(23.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF111827),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

// -----------------------------------------------------------------------------
// 7. SEGMENTED LIGHT SWITCH CONTROL
// -----------------------------------------------------------------------------
@Composable
fun FintechSegmentedControl(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(180.dp)
            .height(38.dp)
            .shadow(4.dp, RoundedCornerShape(19.dp), ambientColor = Color(0x10000000), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(19.dp))
            .background(Color(0xFFF3F4F6))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(19.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (selectedTab == 0) {
                            Modifier
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .background(Color.White)
                        } else Modifier
                    )
                    .clickable { onTabSelected(0) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "User",
                    fontSize = 12.5.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedTab == 0) Color(0xFF111827) else Color(0xFF059669)
                )
            }

            // Admin Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (selectedTab == 1) {
                            Modifier
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .background(Color.White)
                        } else Modifier
                    )
                    .clickable { onTabSelected(1) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = if (selectedTab == 1) Color(0xFFD97706) else Color(0xFF059669),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Admin",
                        fontSize = 12.5.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) Color(0xFFD97706) else Color(0xFF059669)
                    )
                }
            }
        }
    }
}

