package com.meditech.hemav.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Adds a bouncing scale animation when clicked.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Disable ripple if we want just bounce, or keep it. Let's keep ripple for now but maybe null if user wants pure bounce. 
            // Actually, usually bounce replaces ripple or works with it. Let's keep it simple.
            onClick = onClick
        )
}

/**
 * Animates item entry (Slide up + Fade in).
 * Use with LaunchedEffect in a LazyColumn.
 */
@Composable
fun Modifier.animateEnter(
    index: Int,
    delayPerItem: Int = 50
): Modifier {
    val visibleState = remember { MutableTransitionState(false) }
    
    LaunchedEffect(Unit) {
        delay((index * delayPerItem).toLong())
        visibleState.targetState = true
    }

    val alpha by updateTransition(visibleState, label = "Alpha").animateFloat(
        transitionSpec = { tween(durationMillis = 300) },
        label = "Alpha"
    ) { visible -> if (visible) 1f else 0f }

    val translationY by updateTransition(visibleState, label = "TranslationY").animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow) },
        label = "TranslationY"
    ) { visible -> if (visible) 0.dp else 50.dp }

    return this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY.toPx()
    }
}

/**
 * Pulse Effect for critical alerts or scanning
 */
@Composable
fun Modifier.pulseEffect(
    maxScale: Float = 1.2f,
    durationMillis: Int = 1000
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
