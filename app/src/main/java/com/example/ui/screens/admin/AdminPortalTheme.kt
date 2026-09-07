package com.example.ui.screens.admin

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Premium Light Enterprise Design System for Admin Portal.
 * Clean, trustworthy, executive financial control panel.
 */
object AdminTheme {
    // Canvas & Surfaces (Crisp White & Off-White Light Enterprise)
    val Background = Color(0xFFF8FAFC)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFFFFFFFF) // Mapped to clean white for compatibility
    val CardBg = Color(0xFFFFFFFF)
    val CardElevated = Color(0xFFFFFFFF)
    val BorderSubtle = Color(0xFFE2E8F0)
    val BorderStrong = Color(0xFFCBD5E1)
    val BorderGlow = Color(0xFF059669).copy(alpha = 0.2f)

    // Accents & Signals
    val Emerald = Color(0xFF059669)
    val EmeraldLight = Color(0xFFECFDF5)
    val EmeraldDark = Color(0xFF047857)
    val EmeraldGlow = Color(0xFF10B981)

    val Amber = Color(0xFFD97706)
    val AmberLight = Color(0xFFFEF3C7)
    val AmberDark = Color(0xFFB45309)

    val Red = Color(0xFFDC2626)
    val RedLight = Color(0xFFFEE2E2)
    val RedDark = Color(0xFF991B1B)

    val Cyan = Color(0xFF0891B2)
    val CyanLight = Color(0xFFECFEFF)
    val Blue = Color(0xFF2563EB)
    val BlueLight = Color(0xFFDBEAFE)
    val Purple = Color(0xFF7C3AED)
    val Navy = Color(0xFF0F172A)
    val NavyLight = Color(0xFF1E293B)

    // High-Contrast Typography
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val TextMuted = Color(0xFF94A3B8)

    // Gradients (Subtle Enterprise Finishing)
    val HeaderGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
    )

    val GoldButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFD97706), Color(0xFFB45309))
    )

    val EmeraldButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF059669), Color(0xFF047857))
    )

    val RedButtonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFDC2626), Color(0xFFB91C1C))
    )

    val CyanCardGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF0FDF4))
    )
}

