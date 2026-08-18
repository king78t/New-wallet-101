package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BpGoldPrimary
import com.example.ui.theme.BpGreenPrimary

/**
 * Creates an animated linear shimmer brush that smoothly sweeps left to right.
 */
@Composable
fun shimmerBrush(
    isAdminMode: Boolean = false,
    durationMillis: Int = 1200
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer_brush_anim")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = if (isAdminMode) {
        listOf(
            Color(0xFF0D1F15),
            BpGoldPrimary.copy(alpha = 0.40f),
            Color(0xFF0D1F15)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0).copy(alpha = 0.5f),
            Color.White.copy(alpha = 0.92f),
            Color(0xFFE2E8F0).copy(alpha = 0.5f)
        )
    }

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )
}

/**
 * Modifier extension to apply a custom shimmer loading animation with clipping.
 */
@Composable
fun Modifier.shimmerEffect(
    showShimmer: Boolean = true,
    isAdminMode: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier {
    if (!showShimmer) return this
    val brush = shimmerBrush(isAdminMode = isAdminMode)
    return this
        .clip(shape)
        .background(brush)
}

/**
 * Custom, elegant shimmer skeleton for input fields matching GlassTextField design.
 */
@Composable
fun ShimmerGlassTextField(
    modifier: Modifier = Modifier,
    isAdminMode: Boolean = false
) {
    val brush = shimmerBrush(isAdminMode = isAdminMode)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isAdminMode) Color(0x22FFFFFF) else Color(0x77FFFFFF)
            )
            .border(
                width = 1.dp,
                color = if (isAdminMode) BpGoldPrimary.copy(alpha = 0.30f) else BpGreenPrimary.copy(alpha = 0.30f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Skeleton Icon Box
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(brush)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                // Skeleton Label Line
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(11.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Skeleton Input Text Line
                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .height(15.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
            }
        }
    }
}

/**
 * Custom, elegant shimmer skeleton for compact primary action buttons.
 */
@Composable
fun ShimmerCompactButton(
    modifier: Modifier = Modifier,
    widthPercent: Float = 0.70f,
    height: Dp = 48.dp,
    isAdminMode: Boolean = false
) {
    val brush = shimmerBrush(isAdminMode = isAdminMode, durationMillis = 1000)

    Box(
        modifier = modifier
            .fillMaxWidth(widthPercent)
            .height(height)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isAdminMode) BpGoldPrimary.copy(alpha = 0.3f) else BpGreenPrimary.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isAdminMode) {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0F261B), Color(0xFF1E3A2B))
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF16A34A).copy(alpha = 0.85f), BpGreenPrimary.copy(alpha = 0.85f))
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isAdminMode) BpGoldPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Overlay shimmer sweep bar inside button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(brush)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }
    }
}

/**
 * Custom, elegant shimmer skeleton for OTP Digit Fields.
 */
@Composable
fun ShimmerOtpDigits(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush(durationMillis = 1100)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        for (i in 0 until 6) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(
                        width = 1.5.dp,
                        color = BpGreenPrimary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer box shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(brush)
                )

                // Center placeholder circle
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                )
            }
        }
    }
}

/**
 * Custom shimmer skeleton for Country Selector Cards in Register flow.
 */
@Composable
fun ShimmerCountrySelector(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush(durationMillis = 1200)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until 3) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.6f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(brush)
            )
        }
    }
}
