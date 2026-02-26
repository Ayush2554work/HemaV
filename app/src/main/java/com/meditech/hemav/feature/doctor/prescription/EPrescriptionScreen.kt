package com.meditech.hemav.feature.doctor.prescription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meditech.hemav.data.model.Medicine
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.HemaVLoader
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EPrescriptionScreen(
    patientName: String = "Patient",
    patientId: String = "unknown_patient", // TODO: Pass via nav args
    viewModel: EPrescriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var diagnosis by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val medicines = remember { mutableStateListOf<MedicineEntry>() }
    var showAddMedicine by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Ayurvedic medicine suggestions
    val ayurvedicFormulary = listOf(
        "Lohasava (Ayurvedic Iron Tonic)",
        "Punarnava Mandur",
        "Navayasa Churna",
        "Dhatri Loha",
        "Ashwagandha Churna",
        "Shatavari Granules",
        "Triphala Churna",
        "Chandraprabha Vati",
        "Amalaki Rasayana",
        "Guduchi Satva"
    )

    if (viewModel.saveSuccess) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = AyurvedicGreen, modifier = Modifier.size(96.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Prescription Sent!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("The patient will receive the e-prescription.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onSave, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = AyurvedicGreen)) {
                    Text("Done")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E-Prescription") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Patient info badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalCyan.copy(alpha = 0.08f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MedicalCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Patient: $patientName", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Diagnosis
            OutlinedTextField(
                value = diagnosis,
                onValueChange = { diagnosis = it },
                label = { Text("Diagnosis") },
                placeholder = { Text("e.g., Iron Deficiency Anemia (Pandu Roga)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Medicines
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Medicines", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                FilledTonalButton(
                    onClick = { showAddMedicine = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (medicines.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Medication, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No medicines added yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                medicines.forEachIndexed { index, med ->
                    MedicineCard(
                        entry = med,
                        onDelete = { medicines.removeAt(index) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ayurvedic quick-add suggestions
            Text("Ayurvedic Formulary (Quick Add)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ayurvedicFormulary.take(3).forEach { med ->
                    SuggestionChip(
                        onClick = {
                            medicines.add(MedicineEntry(name = med, dosage = "1 tablet", frequency = "Twice daily", duration = "30 days", instructions = "After food with warm water"))
                        },
                        label = { Text(med.split("(").first().trim(), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes") },
                placeholder = { Text("Diet advice, lifestyle changes, follow-up instructions...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.errorMessage != null) {
                Text(
                    text = "Error: ${viewModel.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Send prescription
            Button(
                onClick = {
                    viewModel.savePrescription(
                        context = context,
                        patientId = patientId,
                        patientName = patientName,
                        diagnosis = diagnosis,
                        notes = notes, 
                        medicines = medicines
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = diagnosis.isNotBlank() && medicines.isNotEmpty() && !viewModel.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = AyurvedicGreen)
            ) {
                if (viewModel.isSaving) {
                    HemaVLoader(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Prescription", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Add Medicine Dialog
    if (showAddMedicine) {
        AddMedicineDialog(
            onDismiss = { showAddMedicine = false },
            onAdd = { entry ->
                medicines.add(entry)
                showAddMedicine = false
            }
        )
    }
}

data class MedicineEntry(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = ""
)

@Composable
fun MedicineCard(entry: MedicineEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AyurvedicGreen.copy(alpha = 0.06f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Medication, null, tint = AyurvedicGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Text("${entry.dosage} • ${entry.frequency}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${entry.duration} — ${entry.instructions}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(onDismiss: () -> Unit, onAdd: (MedicineEntry) -> Unit) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Twice daily") }
    var duration by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("After food") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medicine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Medicine Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage (e.g., 1 tablet, 5ml)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                // Frequency chips
                Text("Frequency", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Once daily", "Twice daily", "Thrice daily").forEach { freq ->
                        FilterChip(selected = frequency == freq, onClick = { frequency = freq }, label = { Text(freq, style = MaterialTheme.typography.labelSmall) })
                    }
                }

                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (e.g., 30 days)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                // Instruction chips
                Text("Instructions", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Before food", "After food", "With milk").forEach { instr ->
                        FilterChip(selected = instructions == instr, onClick = { instructions = instr }, label = { Text(instr, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(MedicineEntry(name, dosage, frequency, duration, instructions)) },
                enabled = name.isNotBlank() && dosage.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AyurvedicGreen)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
