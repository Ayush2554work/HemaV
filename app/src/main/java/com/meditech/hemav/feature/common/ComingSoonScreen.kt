package com.meditech.hemav.feature.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meditech.hemav.ui.components.AnimatedLiquidBackground
import com.meditech.hemav.ui.components.GlassCard
import com.meditech.hemav.ui.components.HemaVLoader
import com.meditech.hemav.ui.theme.AyurvedicGreen
import com.meditech.hemav.ui.theme.LocalIsDark
import com.meditech.hemav.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(
    featureName: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HemaV Labs") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(Color(0xFF001A33), Color(0xFF003366), Color.Black)
        } else {
            listOf(NeonCyan, Color.White, Color(0xFFE0F7FA))
        }

        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = liquidColors
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                GlassCard(modifier = Modifier.padding(32.dp)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            null,
                            modifier = Modifier.size(80.dp),
                            tint = AyurvedicGreen
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = featureName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Step into the future of Ayurveda. Our team is hand-crafting this feature to bring you the best wellness experience.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        HemaVLoader()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "ARRIVING SOON",
                            style = MaterialTheme.typography.labelLarge,
                            color = AyurvedicGreen,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}
