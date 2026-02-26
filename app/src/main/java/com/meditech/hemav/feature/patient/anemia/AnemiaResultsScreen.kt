package com.meditech.hemav.feature.patient.anemia

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meditech.hemav.data.model.AnemiaStage
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.FileProvider

/**
 * Displays anemia screening results from AI analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnemiaResultsScreen(
    result: com.meditech.hemav.data.model.AnemiaResult,
    capturedBitmaps: List<Bitmap> = emptyList(),
    isPro: Boolean = false,
    onFindDoctor: () -> Unit = {},
    onBack: () -> Unit
) {
    val stageColor = when (result.stage) {
        AnemiaStage.NORMAL -> NeonGreen
        AnemiaStage.MILD -> MildYellow
        AnemiaStage.MODERATE -> ModerateOrange
        AnemiaStage.SEVERE -> NeonRed
        AnemiaStage.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
        AnemiaStage.INVALID -> ErrorColor
    }

    val liquidBrush = when (result.stage) {
        AnemiaStage.NORMAL -> LiquidGradientSecondary
        AnemiaStage.SEVERE, AnemiaStage.MODERATE -> LiquidGradientDanger
        else -> LiquidGradientPrimary // Fallback blue
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Screening Results", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        ClayCard(
                            modifier = Modifier.size(40.dp), 
                            shape = CircleShape, 
                            elevation = 4.dp, 
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = onBack
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        // Animated Liquid Background matching severity
        val animColors = when (result.stage) {
            AnemiaStage.NORMAL -> listOf(NeonGreen, Color(0xFF00E676), NeonCyan)
            AnemiaStage.SEVERE, AnemiaStage.MODERATE -> listOf(NeonRed, Color(0xFFFF5252), Color(0xFFD50000))
            else -> listOf(NeonCyan, Color(0xFF2979FF), NeonGreen)
        }

        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = animColors
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 150.dp), // Safe zone for nav bar
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Stage Indicator (Clay + Glass)
                ClayCard(
                    modifier = Modifier.fillMaxWidth().animateEnter(0),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Stage Badge with Pulse
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(stageColor.copy(alpha = 0.15f))
                                .border(2.dp, stageColor.copy(alpha = 0.3f), CircleShape)
                                .pulseEffect(maxScale = 1.1f, durationMillis = 1500),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (result.stage) {
                                    AnemiaStage.NORMAL -> Icons.Default.CheckCircle // Tick
                                    AnemiaStage.MILD -> Icons.Default.Warning
                                    AnemiaStage.MODERATE -> Icons.Default.Error
                                    AnemiaStage.SEVERE -> Icons.Default.Dangerous // Cross/Danger
                                    else -> Icons.Default.Help
                                },
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = stageColor
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            result.stage.name,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = stageColor
                        )

                        Text(
                            "Based on WHO Hemoglobin Criteria",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Hemoglobin Stats (Inner Glass)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Est. Hb", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "%.1f g/dL".format(result.hemoglobinEstimate),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.Gray.copy(alpha=0.2f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Confidence", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "%.0f%%".format(result.confidence * 100),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Patient Details Card
                if (result.patientDetailsJson.isNotBlank()) {
                    val patientDetails = try {
                        com.google.gson.Gson().fromJson(
                            result.patientDetailsJson,
                            com.meditech.hemav.data.model.PatientDetails::class.java
                        )
                    } catch (_: Exception) { null }

                    if (patientDetails != null) {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth().animateEnter(1),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, null, tint = CrimsonPrimary, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Patient Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Detail rows
                                val details = mutableListOf<Pair<String, String>>()
                                if (patientDetails.name.isNotBlank()) details.add("Name" to patientDetails.name)
                                if (patientDetails.age > 0) details.add("Age" to "${patientDetails.age} years")
                                if (patientDetails.gender.isNotBlank()) details.add("Gender" to patientDetails.gender)
                                if (patientDetails.ethnicity.isNotBlank()) details.add("Ethnicity" to patientDetails.ethnicity)
                                if (patientDetails.region.isNotBlank()) details.add("Region" to patientDetails.region)
                                if (patientDetails.weight > 0f) details.add("Weight" to "%.1f kg".format(patientDetails.weight))
                                if (patientDetails.dietType.isNotBlank()) details.add("Diet" to patientDetails.dietType)
                                if (patientDetails.knownConditions.isNotBlank()) details.add("Conditions" to patientDetails.knownConditions)
                                if (patientDetails.currentSymptoms.isNotBlank()) details.add("Symptoms" to patientDetails.currentSymptoms)
                                if (patientDetails.menstrualHistory.isNotBlank() && patientDetails.gender == "Female") {
                                    details.add("Menstrual Status" to patientDetails.menstrualHistory)
                                }
                                if (patientDetails.previousAnemia) details.add("Previous Anemia" to "Yes")
                                if (patientDetails.currentMedications.isNotBlank()) details.add("Medications" to patientDetails.currentMedications)

                                details.forEach { (label, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(0.35f)
                                        )
                                        Text(
                                            value,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(0.65f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // AI Explanation (Glass)
                GlassCard(
                    modifier = Modifier.fillMaxWidth().animateEnter(1),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = AccentGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("AI Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            result.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Powered by ${result.providerUsed}", style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(Icons.Default.SmartToy, null, modifier = Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Per-Image Findings (one card per body part)
                if (result.perImageFindings.isNotEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().animateEnter(2),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Camera, null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Per-Image Findings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            val bodyPartIcons = mapOf(
                                "face" to Icons.Default.Face,
                                "tongue" to Icons.Default.MedicalServices,
                                "conjunctiva" to Icons.Default.Visibility,
                                "palm" to Icons.Default.PanTool,
                                "nails" to Icons.Default.Fingerprint
                            )
                            val bodyPartLabels = mapOf(
                                "face" to "Face",
                                "tongue" to "Tongue",
                                "conjunctiva" to "Lower Eyelid",
                                "palm" to "Palm / Wrist",
                                "nails" to "Fingernail Beds"
                            )

                            result.perImageFindings.forEach { (key, finding) ->
                                val icon = bodyPartIcons[key.lowercase()] ?: Icons.Default.Image
                                val label = bodyPartLabels[key.lowercase()] ?: key.replaceFirstChar { it.uppercase() }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(stageColor.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, null, tint = stageColor, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            finding,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3
                                        )
                                    }
                                }
                                if (key != result.perImageFindings.keys.last()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                // Ayurvedic Insights Section
                if (result.ayurvedicInsights.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth().animateEnter(3),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🌿", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Ayurvedic Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            val ayurvedaIcons = mapOf(
                                "dosha_assessment" to "🔥" to "Dosha Assessment",
                                "dietary_recommendations" to "🥗" to "Dietary Recommendations",
                                "herbal_remedies" to "🌿" to "Herbal Remedies",
                                "lifestyle_tips" to "🧘" to "Lifestyle Tips",
                                "home_remedies" to "🏠" to "Home Remedies"
                            )

                            result.ayurvedicInsights.forEach { (key, value) ->
                                val (emoji, label) = when (key.lowercase()) {
                                    "dosha_assessment" -> "🔥" to "Dosha Assessment"
                                    "dietary_recommendations" -> "🥗" to "Dietary Recommendations"
                                    "herbal_remedies" -> "🌿" to "Herbal Remedies"
                                    "lifestyle_tips" -> "🧘" to "Lifestyle Tips"
                                    "home_remedies" -> "🏠" to "Home Remedies"
                                    else -> "💊" to key.replace("_", " ").replaceFirstChar { it.uppercase() }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2E7D32).copy(alpha = 0.08f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(emoji, style = MaterialTheme.typography.titleSmall)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            value,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Disclaimer
                Surface(
                    modifier = Modifier.fillMaxWidth().animateEnter(4),
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGold.copy(alpha = 0.1f)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Info, null, tint = AccentGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "This is an AI-assisted screening, not a medical diagnosis. Please consult a healthcare professional.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Action Buttons
                val context = androidx.compose.ui.platform.LocalContext.current
                val scope = rememberCoroutineScope()
                var isGeneratingPdf by remember { mutableStateOf(false) }
                var showProLockDialog by remember { mutableStateOf(false) }

                if (showProLockDialog) {
                    AlertDialog(
                        onDismissRequest = { showProLockDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AccentGold, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("HemaV Pro Feature", fontWeight = FontWeight.Bold, color = AccentGold)
                            }
                        },
                        text = {
                            Text(
                                "Detailed PDF reports are an exclusive feature of HemaV Pro.\n\nSimulating Razorpay Checkout:\nUpgrade to Pro (₹999/year) to unlock unlimited scans and reports.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { showProLockDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.White)
                            ) {
                                Text("Upgrade Now (Simulated)")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showProLockDialog = false }) {
                                Text("Maybe Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                }

                if (isGeneratingPdf) {
                    HemaVLoader(modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    ClayCard(
                        onClick = { 
                            if (isPro) {
                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    isGeneratingPdf = true
                                    try {
                                        val bitmaps = capturedBitmaps.ifEmpty {
                                            result.imageUrls.mapNotNull { urlString ->
                                                try {
                                                    val url = java.net.URL(urlString)
                                                    android.graphics.BitmapFactory.decodeStream(url.openStream())
                                                } catch (e: Exception) { e.printStackTrace(); null }
                                            }
                                        }
                                        val file = com.meditech.hemav.util.PdfReportGenerator.generateReport(context, result, "Patient", bitmaps)
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            if (file != null) {
                                                android.widget.Toast.makeText(context, "PDF saved: ${file.name}", android.widget.Toast.LENGTH_LONG).show()
                                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                                intent.setDataAndType(uri, "application/pdf")
                                                intent.flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                try { context.startActivity(intent) } catch (e: Exception) { 
                                                    android.widget.Toast.makeText(context, "No PDF viewer found", android.widget.Toast.LENGTH_SHORT).show() 
                                                }
                                            } else {
                                                android.widget.Toast.makeText(context, "Failed to generate PDF", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } finally { isGeneratingPdf = false }
                                }
                            } else {
                                showProLockDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).animateEnter(3),
                        shape = RoundedCornerShape(16.dp),
                        color = NeonCyan,
                        elevation = 8.dp
                    ) {
                        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download PDF Report", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ClayCard(
                    onClick = onFindDoctor,
                    modifier = Modifier.fillMaxWidth().height(56.dp).animateEnter(4),
                    shape = RoundedCornerShape(16.dp),
                    color = NeonGreen,
                    elevation = 8.dp
                ) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Find Ayurvedic Doctor", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
