package com.meditech.hemav.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// --- Modern Neon Palette (Liquid/Glass) ---
val NeonCyan = Color(0xFF00E5FF)
val NeonGreen = Color(0xFF00C853)
val NeonRed = Color(0xFFFF1744)

// Dark/Light Bases
val BackgroundDark = Color(0xFF121212)
val BackgroundLight = Color(0xFFF0F2F5)
val SurfaceDark = Color(0xFF1E1E1E)
val SurfaceLight = Color(0xFFF5F7FA)

// Gradients (Liquid Effect)
val LiquidGradientPrimary = Brush.linearGradient(
    colors = listOf(NeonCyan, Color(0xFF2979FF))
)
val LiquidGradientSecondary = Brush.linearGradient(
    colors = listOf(NeonGreen, Color(0xFF00E676))
)
val LiquidGradientDanger = Brush.linearGradient(
    colors = listOf(NeonRed, Color(0xFFFF5252))
)

// Mirror Finish (Metallic/Reflective)
val MirrorGreyBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF8F9FA),
        Color(0xFFE9ECEF),
        Color(0xFFDEE2E6),
        Color(0xFFF8F9FA)
    )
)

// Glassmorphism
val GlassWhite = Color.White.copy(alpha = 0.15f)
val GlassWhiteStrong = Color.White.copy(alpha = 0.3f)
val GlassBorder = Color.White.copy(alpha = 0.4f)
val GlassDark = Color.Black.copy(alpha = 0.3f)

// Claymorphism Shadows
val ShadowLight = Color.White
val ShadowDark = Color(0xFFAEC0CE)

// Functional Mapping
val CrimsonPrimary = NeonRed
val GreenPrimary = NeonGreen
val CyanPrimary = NeonCyan
val CrimsonVibrant = Color(0xFFFF5252)
val GreenVibrant = Color(0xFF69F0AE)
val CyanVibrant = Color(0xFF18FFFF)
val CrimsonDark = Color(0xFFD50000)

val OnSurfaceLight = Color(0xFF121212) // Darker for better contrast
val OnSurfaceDark = Color(0xFFE6E1E5)
val TextSecondaryLight = Color(0xFF424242) // Sharper grey
val TextSecondaryDark = Color(0xFFCAC4D0)

val GlassWhiteLight = Color.White.copy(alpha = 0.7f) // Much more opaque for readability
val GlassBorderLight = Color.Black.copy(alpha = 0.12f) // Subtle but visible dark border

val ErrorColor = NeonRed
val AyurvedicGreen = NeonGreen
val MedicalCyan = NeonCyan
val AccentGold = Color(0xFFFFD700)
val HealthyGreen = NeonGreen
val MildYellow = Color(0xFFFFD600)
val ModerateOrange = Color(0xFFFF6D00)
val SevereRed = NeonRed

// Light Variants (restored for compatibility)
val CrimsonLight = Color(0xFFFF8A80)
val AyurvedicGreenLight = Color(0xFFB9F6CA)
val MedicalCyanLight = Color(0xFF84FFFF)
