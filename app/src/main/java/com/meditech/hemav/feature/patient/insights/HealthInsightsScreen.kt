package com.meditech.hemav.feature.patient.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditech.hemav.data.model.AnemiaResult
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthInsightsScreen(
    isPro: Boolean = false,
    onNavigateToPremium: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: HealthInsightsViewModel = viewModel()
) {
    val scanHistory by viewModel.scanHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInsights()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Insights") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(Color(0xFF003300), Color(0xFF0E2E0E), Color.Black)
        } else {
            listOf(MedicalCyan, AyurvedicGreen, Color(0xFF81C784))
        }

        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = liquidColors
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    HemaVLoader()
                }
            } else if (scanHistory.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyHistoryView()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Hemoglobin Trend Card
                    TrendChartCard(scanHistory)

                    // Summary Card
                    AIGeneratedSummaryCard(scanHistory)

                    // Latest stats
                    StatsGrid(scanHistory.last())

                    // HemaV Pro CTA for Free Users
                    if (!isPro) {
                        Spacer(modifier = Modifier.height(16.dp))
                        com.meditech.hemav.ui.components.GlassCard(
                            modifier = Modifier.fillMaxWidth().animateEnter(3),
                            shape = RoundedCornerShape(24.dp),
                            backgroundColor = AccentGold.copy(alpha = if (isDark) 0.15f else 0.1f),
                            borderColor = AccentGold.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToPremium() }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro", tint = AccentGold, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Get Personalized Diet Plans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentGold)
                                    Text("Upgrade to HemaV Pro for AI-driven nutrition & lifestyle guidance.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistoryView() {
    GlassCard(modifier = Modifier.padding(32.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.QueryStats, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Scan History Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Complete an anemia scan to see your health trends and AI insights.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun TrendChartCard(history: List<AnemiaResult>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().animateEnter(0),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, null, tint = AyurvedicGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hemoglobin Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Simple Line Chart
            HemoglobinChart(
                data = history.map { it.hemoglobinEstimate },
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend / Timeframe
            Text(
                "Tracking over last ${history.size} scans",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HemoglobinChart(data: List<Float>, modifier: Modifier = Modifier) {
    val primaryColor = AyurvedicGreen
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    
    // Normalize data: min around 5, max around 15
    val minVal = 5f
    val maxVal = 16f
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = if (data.size > 1) width / (data.size - 1) else width
        
        // Draw Grid Lines
        for (i in 0..4) {
            val y = height - (i * height / 4)
            drawLine(gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1.dp.toPx())
        }

        if (data.size < 2) {
             // Just a point or flat line
             return@Canvas
        }

        // Generate Path
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            // Inverse Y since 0 is top
            val y = height - ((value - minVal) / (maxVal - minVal) * height)
            
            if (index == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw points
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - ((value - minVal) / (maxVal - minVal) * height)
            drawCircle(primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
fun AIGeneratedSummaryCard(history: List<AnemiaResult>) {
    val latest = history.last()
    val isImproving = history.size > 1 && latest.hemoglobinEstimate > history[history.size - 2].hemoglobinEstimate
    
    GlassCard(
        modifier = Modifier.fillMaxWidth().animateEnter(1),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AI Health Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(
                        if (isImproving) AyurvedicGreen.copy(alpha = 0.2f) else AccentGold.copy(alpha = 0.2f)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isImproving) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null,
                        tint = if (isImproving) AyurvedicGreen else AccentGold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        if (isImproving) "Condition Improving" else "Stable Trend",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Based on your last ${history.size} checks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Your hemoglobin levels are currently at ${latest.hemoglobinEstimate} g/dL. " +
                "According to Ayurvedic principles, focusing on Pitta-balancing foods and " +
                "natural iron sources like Amla and Pomegranate can further support your recovery.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun StatsGrid(latest: AnemiaResult) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().animateEnter(2)) {
        StatBox("Current Hb", "${latest.hemoglobinEstimate}", "g/dL", modifier = Modifier.weight(1f))
        StatBox("Confidence", "${(latest.confidence * 100).toInt()}%", "AI Accuracy", modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatBox(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    ClayCard(modifier = modifier, shape = RoundedCornerShape(20.dp), elevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AyurvedicGreen)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
