package com.meditech.hemav.feature.patient.medication

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditech.hemav.data.model.Medicine
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    onBack: () -> Unit,
    viewModel: PatientMedicationsViewModel = viewModel()
) {
    val context = LocalContext.current
    val prescriptions = viewModel.prescriptions
    val isLoading = viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.loadMedications(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Medications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadMedications(context) }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(Color(0xFF003300), Color(0xFF1B5E20), Color(0xFF001100))
        } else {
             listOf(AyurvedicGreen, NeonGreen, Color(0xFFB9F6CA))
        }

        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = liquidColors
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    HemaVLoader()
                }
            } else if (prescriptions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    GlassCard {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Medication, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No active medications found.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 150.dp), // Safe zone for floating nav
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HemaVCalendarStrip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    prescriptions.forEachIndexed { index, prescription ->
                        item {
                            Text(
                                "Prescribed by ${prescription.doctorName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp).animateEnter(index * 2)
                            )
                        }
                        items(prescription.medicines) { medicine ->
                            MedicationItemCard(medicine, modifier = Modifier.animateEnter(index * 2 + 1))
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationItemCard(medicine: Medicine, modifier: Modifier = Modifier) {
    ClayCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AyurvedicGreen.copy(alpha = 0.15f))
                    .border(1.dp, AyurvedicGreen.copy(alpha=0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                 Icon(Icons.Default.Medication, null, tint = AyurvedicGreen, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(medicine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("${medicine.dosage} • ${medicine.frequency}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(medicine.instructions, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
