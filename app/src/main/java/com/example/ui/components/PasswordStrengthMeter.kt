package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.validation.PasswordStrength

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PasswordStrengthMeter(
    strength: PasswordStrength,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = strength.percentage,
        animationSpec = tween(durationMillis = 300),
        label = "strength_progress"
    )

    val animatedColor by animateColorAsState(
        targetValue = strength.color,
        animationSpec = tween(durationMillis = 300),
        label = "strength_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Label & Score Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password Strength",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4B5563)
            )

            Text(
                text = strength.label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0.02f, 1.0f))
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(animatedColor)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Criteria Pills FlowRow
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StrengthChip(label = "8+ Chars", isMet = strength.hasMinLength)
            StrengthChip(label = "A-Z", isMet = strength.hasUppercase)
            StrengthChip(label = "a-z", isMet = strength.hasLowercase)
            StrengthChip(label = "0-9", isMet = strength.hasDigit)
            StrengthChip(label = "Special", isMet = strength.hasSpecialChar)
        }
    }
}

@Composable
private fun StrengthChip(
    label: String,
    isMet: Boolean
) {
    val bgColor = if (isMet) Color(0xFFD1FAE5) else Color(0xFFF3F4F6)
    val textColor = if (isMet) Color(0xFF065F46) else Color(0xFF6B7280)
    val iconColor = if (isMet) Color(0xFF059669) else Color(0xFF9CA3AF)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = if (isMet) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
