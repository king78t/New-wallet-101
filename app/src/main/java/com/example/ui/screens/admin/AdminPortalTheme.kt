package com.example.ui.screens.admin

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AdminTheme {
    // Canvas & Surfaces (Executive Dark Navy & Charcoal)
    val Background = Color(0xFF090D16)
    val SurfaceDark = Color(0xFF111827)
    val CardBg = Color(0xFF1E293B)
    val CardElevated = Color(0xFF1F2937)
    val BorderSubtle = Color(0xFF334155)
    val BorderGlow = Color(0xFF3B82F6).copy(alpha = 0.35f)

    // Accents & Signals
    val Emerald = Color(0xFF10B981)
    val EmeraldDark = Color(0xFF065F46)
    val EmeraldGlow = Color(0xFF34D399)

    val Amber = Color(0xFFF59E0B)
    val AmberDark = Color(0xFF78350F)

    val Red = Color(0xFFEF4444)
    val RedDark = Color(0xFF7F1D1D)

    val Cyan = Color(0xFF06B6D4)
    val Blue = Color(0xFF3B82F6)
    val Purple = Color(0xFF8B5CF6)

    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    // Gradients
    val HeaderGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0F172A))
    )

    val GoldButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    )

    val EmeraldButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF10B981), Color(0xFF059669))
    )

    val RedButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))
    )

    val CyanCardGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF132338))
    )
}
