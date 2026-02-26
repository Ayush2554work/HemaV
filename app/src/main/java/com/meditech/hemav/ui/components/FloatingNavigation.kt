package com.meditech.hemav.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import com.meditech.hemav.ui.theme.*

data class NavigationItem(
    val index: Int,
    val icon: ImageVector,
    val label: String,
    val color: Color
)

@Composable
fun FloatingBottomNavigation(
    items: List<NavigationItem>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    // Calculate width of the navbar to be slightly less than screen width
    val navBarWidth = screenWidth - 32.dp
    val itemWidth = navBarWidth / items.size

    // Animate the offset of the selection blob
    val indicatorOffset by animateDpAsState(
        targetValue = itemWidth * selectedItem,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessLow
        ),
        label = "LiquidBlobPos"
    )

    val selectedColor = items.getOrNull(selectedItem)?.color ?: NeonCyan

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .height(80.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(40.dp),
                spotColor = selectedColor.copy(alpha = 0.5f),
                ambientColor = Color.Black.copy(alpha = 0.2f)
            )
    ) {
        val isDark = LocalIsDark.current
        val glassBg = if (isDark) Color.Black.copy(alpha = 0.8f) else GlassWhite.copy(alpha = 0.82f)
        val glassBorder = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.5f)

        // Glass Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(40.dp))
                .background(glassBg)
                .then(if (android.os.Build.VERSION.SDK_INT >= 31) Modifier.blur(20.dp) else Modifier)
                .border(1.dp, glassBorder, RoundedCornerShape(40.dp))
        )

        // Content Row
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Liquid Blob Indicator
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
               Box(
                   modifier = Modifier
                       .fillMaxSize()
                       .clip(RoundedCornerShape(32.dp))
                       .background(
                           Brush.linearGradient(
                               colors = listOf(
                                   selectedColor.copy(alpha = 0.2f),
                                   selectedColor.copy(alpha = 0.1f)
                               )
                           )
                       )
                       .border(
                           width = 1.dp,
                           brush = Brush.verticalGradient(
                               listOf(
                                   selectedColor.copy(alpha = 0.5f),
                                   Color.Transparent
                               )
                           ),
                           shape = RoundedCornerShape(32.dp)
                       )
               )
            }

            // Items
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = selectedItem == item.index
                    val interactionSource = remember { MutableInteractionSource() }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onItemSelected(item.index) }
                            )
                            // Add bounce effect on click
                            .bounceClick(onClick = { onItemSelected(item.index) }),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) item.color else TextSecondaryLight,
                                modifier = Modifier.size(26.dp)
                            )
                            
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = item.color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
