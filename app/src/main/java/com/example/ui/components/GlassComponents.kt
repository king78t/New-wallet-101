package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BpErrorRed
import com.example.ui.theme.BpGoldPrimary
import com.example.ui.theme.BpGoldSecondary
import com.example.ui.theme.BpGreenDeepBg
import com.example.ui.theme.BpGreenEmerald
import com.example.ui.theme.BpGreenLight
import com.example.ui.theme.BpGreenPrimary

/**
 * Animated Ambient Glass Background with floating blurred emerald orbs,
 * luxury light streaks, and soft depth layer.
 */
@Composable
fun AnimatedGlassBackground(
    isAdminMode: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_orb_anim")
    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )
    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isAdminMode) {
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
        // Floating ambient glowing orbs
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(70.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (isAdminMode) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BpGoldPrimary.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(canvasWidth * 0.2f + floatOffset1 * 2, canvasHeight * 0.15f + floatOffset2),
                        radius = canvasWidth * 0.45f
                    ),
                    center = Offset(canvasWidth * 0.2f + floatOffset1 * 2, canvasHeight * 0.15f + floatOffset2),
                    radius = canvasWidth * 0.45f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BpGreenPrimary.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(canvasWidth * 0.8f + floatOffset2, canvasHeight * 0.7f + floatOffset1),
                        radius = canvasWidth * 0.5f
                    ),
                    center = Offset(canvasWidth * 0.8f + floatOffset2, canvasHeight * 0.7f + floatOffset1),
                    radius = canvasWidth * 0.5f
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BpGreenLight.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(canvasWidth * 0.15f + floatOffset1 * 2, canvasHeight * 0.2f + floatOffset2),
                        radius = canvasWidth * 0.5f
                    ),
                    center = Offset(canvasWidth * 0.15f + floatOffset1 * 2, canvasHeight * 0.2f + floatOffset2),
                    radius = canvasWidth * 0.5f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BpGreenEmerald.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(canvasWidth * 0.85f + floatOffset2, canvasHeight * 0.65f + floatOffset1),
                        radius = canvasWidth * 0.55f
                    ),
                    center = Offset(canvasWidth * 0.85f + floatOffset2, canvasHeight * 0.65f + floatOffset1),
                    radius = canvasWidth * 0.55f
                )
            }
        }

        // Ambient glass light streak
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val path = Path().apply {
                moveTo(0f, height * 0.1f)
                lineTo(width, height * 0.3f)
                lineTo(width, height * 0.35f)
                lineTo(0f, height * 0.15f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isAdminMode) 0.03f else 0.12f),
                        Color.White.copy(alpha = 0f)
                    )
                )
            )
        }

        content()
    }
}

/**
 * Main Glassmorphism Card suspended with 3D elevation shadow & glass border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    borderWidth: Dp = 1.5.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glass_card_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardY"
    )

    Box(
        modifier = modifier
            .offset(y = floatY.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = BpGreenPrimary.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.88f),
                        Color.White.copy(alpha = 0.72f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        BpGreenPrimary.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/**
 * Dark Glassmorphism Card tailored specifically for the Exclusive Admin Portal.
 */
@Composable
fun DarkGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dark_card_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "adminCardY"
    )

    Box(
        modifier = modifier
            .offset(y = floatY.dp)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = BpGoldPrimary.copy(alpha = 0.25f),
                spotColor = BpGreenPrimary.copy(alpha = 0.20f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFAFFFFFF),
                        Color(0xF5FFFFFF)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        BpGoldPrimary.copy(alpha = 0.80f),
                        BpGreenPrimary.copy(alpha = 0.40f),
                        BpGoldPrimary.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/**
 * Compact or Full-Width Primary Fintech Button.
 */
@Composable
fun CompactPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    widthPercent: Float = 1.0f,
    height: Dp = 52.dp,
    isLoading: Boolean = false,
    isAdminMode: Boolean = false,
    icon: ImageVector? = null
) {
    if (isLoading) {
        ShimmerCompactButton(
            modifier = modifier,
            widthPercent = widthPercent,
            height = height,
            isAdminMode = isAdminMode
        )
    } else {
        var isPressed by remember { mutableStateOf(false) }
        val scaleAnimation by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1.0f,
            animationSpec = tween(durationMillis = 100),
            label = "btn_press"
        )

        Box(
            modifier = modifier
                .fillMaxWidth(widthPercent)
                .height(height)
                .graphicsLayer(scaleX = scaleAnimation, scaleY = scaleAnimation)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = if (isAdminMode) BpGoldPrimary.copy(alpha = 0.5f) else Color(0xFF22C55E).copy(alpha = 0.5f),
                    spotColor = if (isAdminMode) BpGoldSecondary else Color(0xFF16A34A)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = if (isAdminMode) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF059669),
                                BpGoldPrimary
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF22C55E),
                                Color(0xFF16A34A),
                                Color(0xFF059669)
                            )
                        )
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isAdminMode) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = if (isAdminMode) Color.Black else Color.White,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Compact Secondary Glass Button with delicate border & glass reflection.
 */
@Composable
fun CompactSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAdminMode: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isAdminMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.55f)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        if (isAdminMode) BpGoldPrimary.copy(alpha = 0.5f) else BpGreenPrimary.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isAdminMode) BpGoldPrimary else BpGreenDeepBg,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Glass Text Field with green focus glow, error shake animation, and 16dp radius.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isAdminMode: Boolean = false
) {
    if (isLoading) {
        ShimmerGlassTextField(
            modifier = modifier,
            isAdminMode = isAdminMode
        )
    } else {
        val shakeOffset = remember { Animatable(0f) }

        LaunchedEffect(isError) {
            if (isError) {
                repeat(3) {
                    shakeOffset.animateTo(10f, animationSpec = tween(50))
                    shakeOffset.animateTo(-10f, animationSpec = tween(50))
                }
                shakeOffset.animateTo(0f, animationSpec = tween(50))
            }
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .offset(x = shakeOffset.value.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = label,
                        color = if (isAdminMode) Color.White.copy(alpha = 0.7f) else Color(0xFF475569),
                        fontSize = 13.5.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isError) BpErrorRed else if (isAdminMode) BpGoldPrimary else BpGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = trailingIcon,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = true,
                isError = isError,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isAdminMode) Color(0x33FFFFFF) else Color(0x99FFFFFF),
                    unfocusedContainerColor = if (isAdminMode) Color(0x1AFFFFFF) else Color(0x66FFFFFF),
                    errorContainerColor = Color(0x1AFE2C55),
                    focusedBorderColor = if (isAdminMode) BpGoldPrimary else BpGreenPrimary,
                    unfocusedBorderColor = if (isAdminMode) Color.White.copy(alpha = 0.2f) else BpGreenPrimary.copy(alpha = 0.25f),
                    errorBorderColor = BpErrorRed,
                    focusedLabelColor = if (isAdminMode) BpGoldPrimary else BpGreenPrimary,
                    unfocusedLabelColor = if (isAdminMode) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                    focusedTextColor = if (isAdminMode) Color.White else Color(0xFF0F172A),
                    unfocusedTextColor = if (isAdminMode) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A),
                    cursorColor = if (isAdminMode) BpGoldPrimary else BpGreenPrimary
                )
            )

            if (isError && !errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage,
                    color = BpErrorRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

/**
 * User / Admin Glass Segmented Switch Tabs
 */
@Composable
fun GlassSegmentedControl(
    selectedTab: Int, // 0 for User, 1 for Admin
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(220.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.Black.copy(alpha = 0.06f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(3.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // User Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(19.dp))
                    .then(
                        if (selectedTab == 0) {
                            Modifier.background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(BpGreenPrimary, Color(0xFF16A34A))
                                )
                            )
                        } else {
                            Modifier.background(color = Color.Transparent)
                        }
                    )
                    .clickable { onTabSelected(0) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "User",
                    color = if (selectedTab == 0) Color.White else Color(0xFF64748B),
                    fontSize = 14.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                )
            }

            // Admin Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(19.dp))
                    .then(
                        if (selectedTab == 1) {
                            Modifier.background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(BpGoldPrimary, BpGoldSecondary)
                                )
                            )
                        } else {
                            Modifier.background(color = Color.Transparent)
                        }
                    )
                    .clickable { onTabSelected(1) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Admin",
                    color = if (selectedTab == 1) Color.Black else Color(0xFF64748B),
                    fontSize = 14.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Luxury BP Wallet Logo Badge Header with circular glass container and glow.
 */
@Composable
fun GlassLogoHeader(
    isAdminMode: Boolean = false,
    title: String = "BP WALLET",
    subtitle: String = "OFFICIAL WALLET & DEPOSIT SERVICE"
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = if (isAdminMode) BpGoldPrimary.copy(alpha = 0.5f) else BpGreenPrimary.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    brush = if (isAdminMode) {
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E3A2B), Color(0xFF09140D))
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFDCFCE7), Color(0xFFF0FDF4))
                        )
                    }
                )
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = if (isAdminMode) {
                            listOf(BpGoldPrimary, BpGreenPrimary, BpGoldPrimary)
                        } else {
                            listOf(BpGreenPrimary, BpGreenLight, BpGreenPrimary)
                        }
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAdminMode) BpGoldPrimary else BpGreenPrimary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "b",
                    color = if (isAdminMode) Color.Black else Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isAdminMode) BpGoldPrimary else Color(0xFF0F172A),
            letterSpacing = 1.sp
        )

        Text(
            text = subtitle,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAdminMode) Color.White.copy(alpha = 0.6f) else BpGreenEmerald,
            letterSpacing = 1.2.sp
        )
    }
}
