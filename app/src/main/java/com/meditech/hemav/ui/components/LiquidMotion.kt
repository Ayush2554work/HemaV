package com.meditech.hemav.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.meditech.hemav.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Creates an iOS 18-style animated liquid background with floating gradient blobs.
 * @param colors List of colors to use for the blobs.
 * @param speedMultiplier Controls how fast the blobs move.
 */
@Composable
fun AnimatedLiquidBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(NeonCyan, NeonGreen, Color(0xFF2979FF)),
    speedMultiplier: Float = 1.0f,
    content: @Composable BoxScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

    // Convert to px for offset calculations
    val widthPx = with(density) { screenWidth.toPx() }
    val heightPx = with(density) { screenHeight.toPx() }
    
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidMotion")

    // Blob 1 Animation (Top Left -> Center)
    val blob1Offset by infiniteTransition.animateValue(
        initialValue = Offset(0f, 0f),
        targetValue = Offset(widthPx * 0.3f, heightPx * 0.2f),
        typeConverter = TwoWayConverter(
            convertToVector = { AnimationVector2D(it.x, it.y) },
            convertFromVector = { Offset(it.v1, it.v2) }
        ),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (5000 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1Pos"
    )

    // Blob 2 Animation (Bottom Right -> Center)
    val blob2Offset by infiniteTransition.animateValue(
        initialValue = Offset(widthPx, heightPx),
        targetValue = Offset(widthPx * 0.6f, heightPx * 0.7f),
        typeConverter = TwoWayConverter(
            convertToVector = { AnimationVector2D(it.x, it.y) },
            convertFromVector = { Offset(it.v1, it.v2) }
        ),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (7000 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob2Pos"
    )
    
     // Blob 3 Animation (Middle -> Out)
    val blob3Scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (4000 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob3Scale"
    )

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Blob 1 - Optimized
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(blob1Offset.x.toInt(), blob1Offset.y.toInt()) }
                .scale(1.2f)
                .fillMaxSize(0.5f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors[0].copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .then(if (android.os.Build.VERSION.SDK_INT >= 31) Modifier.blur(30.dp) else Modifier)
        )

        // Blob 2 - Optimized
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(blob2Offset.x.toInt() - widthPx.toInt()/2, blob2Offset.y.toInt() - heightPx.toInt()/2) }
                .scale(1.4f)
                .fillMaxSize(0.6f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors[1].copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .then(if (android.os.Build.VERSION.SDK_INT >= 31) Modifier.blur(40.dp) else Modifier)
        )

        val isDark = LocalIsDark.current
        val overlayColor = if (isDark) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor)
        )
        
        content()
    }
}
