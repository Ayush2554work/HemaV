package com.meditech.hemav.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.staticCompositionLocalOf

val LocalThemeToggle = staticCompositionLocalOf { {} }
val LocalIsDark = staticCompositionLocalOf { false }
private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color.Black,
    primaryContainer = GreenVibrant,
    onPrimaryContainer = Color.Black,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = CyanVibrant,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = NeonRed,
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color.White,
    primaryContainer = CrimsonVibrant,
    onPrimaryContainer = Color.White,
    secondary = GreenPrimary,
    onSecondary = Color.White,
    secondaryContainer = GreenVibrant,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = Color.White, // Pure white for maximum contrast
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFF5F7FA),
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorColor,
)

@Composable
fun HemaVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HemaVTypography,
        content = content
    )
}
