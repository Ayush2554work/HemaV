package com.meditech.hemav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.meditech.hemav.ui.theme.*

/**
 * Liquid Background Modifier
 * Adds a subtle animated gradient mesh feel (static for now)
 */
fun Modifier.liquidBackground(
    brush: Brush = LiquidGradientPrimary,
    alpha: Float = 1.0f,
    shape: Shape = RoundedCornerShape(0.dp)
): Modifier = this.background(brush, shape, alpha)

/**
 * Enhanced Clay Card
 * Soft, inflated 3D look with inner sorting and outer shadows.
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDark.current

    // Inner highlight (top-left) + Inner shadow (bottom-right)
    val innerShadowBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.05f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.6f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.05f)
            )
        )
    }
    
    val spotColor = if (isDark) Color.Black else ShadowDark
    val ambientColor = if (isDark) NeonGreen.copy(alpha=0.3f) else ShadowLight

    Surface(
        modifier = modifier
            .shadow(elevation, shape, spotColor = spotColor, ambientColor = ambientColor)
            .then(
                if (onClick != null) {
                    Modifier.bounceClick(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    })
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = if (isDark) color else Color.Transparent, // Use transparent in light mode to show mirror brush
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (!isDark && color == MaterialTheme.colorScheme.surface) {
                        Modifier.background(MirrorGreyBrush)
                    } else {
                        Modifier
                    }
                )
                .background(innerShadowBrush)
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Enhanced Glass Card
 * Frosty look with white border and blur simulation.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color.Unspecified, // Changed default to allow logic override
    borderColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDark.current
    
    val actualBorder = if (borderColor != Color.Unspecified) borderColor else {
        if (isDark) Color.White.copy(alpha = 0.1f) else GlassBorderLight
    }
    
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.bounceClick(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    })
                } else {
                    Modifier
                }
            )
            .then(
                if (backgroundColor != Color.Unspecified) {
                    Modifier.background(backgroundColor)
                } else if (isDark) {
                    Modifier.background(Color.Black.copy(alpha = 0.4f))
                } else {
                    Modifier.background(MirrorGreyBrush)
                }
            )
            .border(1.dp, actualBorder, shape)
            .padding(16.dp),
        content = content
    )
}
