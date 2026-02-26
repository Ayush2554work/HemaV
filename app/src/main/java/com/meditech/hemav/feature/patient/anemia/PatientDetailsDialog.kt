package com.meditech.hemav.feature.patient.anemia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.meditech.hemav.data.model.PatientDetails
import com.meditech.hemav.ui.theme.*

/**
 * Full-screen dialog to collect patient details before anemia screening.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsDialog(
    onDismiss: () -> Unit,
    onConfirm: (PatientDetails) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var ethnicity by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var dietType by remember { mutableStateOf("") }
    var knownConditions by remember { mutableStateOf("") }
    var currentSymptoms by remember { mutableStateOf("") }
    var menstrualHistory by remember { mutableStateOf("") }
    var previousAnemia by remember { mutableStateOf(false) }
    var currentMedications by remember { mutableStateOf("") }

    val genderOptions = listOf("Male", "Female", "Other")
    val ethnicityOptions = listOf("South Asian", "African", "Caucasian", "East Asian", "Hispanic", "Middle Eastern", "Other")
    val dietOptions = listOf("Vegetarian", "Non-Vegetarian", "Vegan", "Eggetarian")
    val menstrualOptions = listOf("N/A", "Regular", "Irregular", "Heavy Bleeding", "Pregnant", "Postpartum", "Menopausal")

    val isFormValid = name.isNotBlank() && age.isNotBlank()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = CrimsonPrimary,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Patient Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, "Close", tint = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                        Text(
                            "Fill in details for accurate AI analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                        .padding(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // === REQUIRED SECTION ===
                    SectionLabel("Required Information", Icons.Default.Person)

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Age
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) age = it },
                        label = { Text("Age *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // === DEMOGRAPHICS ===
                    SectionLabel("Demographics", Icons.Default.Public)

                    // Ethnicity
                    ChipSelector(
                        label = "Ethnicity",
                        options = ethnicityOptions,
                        selected = ethnicity,
                        onSelect = { ethnicity = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Region
                    OutlinedTextField(
                        value = region,
                        onValueChange = { region = it },
                        label = { Text("Region / Location") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        placeholder = { Text("e.g., Northern India, Sub-Saharan Africa") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // === MEDICAL DETAILS ===
                    SectionLabel("Medical Details", Icons.Default.MedicalServices)

                    // Weight & Diet row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { if (it.length <= 5) weight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(0.4f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        ChipSelector(
                            label = "Diet",
                            options = dietOptions,
                            selected = dietType,
                            onSelect = { dietType = it },
                            modifier = Modifier.weight(0.6f)
                        )
                    }

                    // Known Conditions
                    OutlinedTextField(
                        value = knownConditions,
                        onValueChange = { knownConditions = it },
                        label = { Text("Known Medical Conditions") },
                        placeholder = { Text("Diabetes, Thyroid, Thalassemia...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Current Symptoms
                    OutlinedTextField(
                        value = currentSymptoms,
                        onValueChange = { currentSymptoms = it },
                        label = { Text("Current Symptoms") },
                        placeholder = { Text("Fatigue, dizziness, pale skin, breathlessness...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Menstrual History (only if female)
                    if (gender == "Female") {
                        ChipSelector(
                            label = "Menstrual Status",
                            options = menstrualOptions,
                            selected = menstrualHistory,
                            onSelect = { menstrualHistory = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Previous Anemia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Previously diagnosed with anemia?", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = previousAnemia,
                            onCheckedChange = { previousAnemia = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = CrimsonPrimary)
                        )
                    }

                    // Current Medications
                    OutlinedTextField(
                        value = currentMedications,
                        onValueChange = { currentMedications = it },
                        label = { Text("Current Medications") },
                        placeholder = { Text("Iron supplements, folic acid, any prescriptions...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // === GENDER (at end) ===
                    SectionLabel("Gender", Icons.Default.Person)

                    ChipSelector(
                        label = "Gender",
                        options = genderOptions,
                        selected = gender,
                        onSelect = { gender = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bottom Action Buttons
                Surface(tonalElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                onConfirm(
                                    PatientDetails(
                                        name = name.trim(),
                                        age = age.toIntOrNull() ?: 0,
                                        gender = gender,
                                        ethnicity = ethnicity,
                                        region = region.trim(),
                                        weight = weight.toFloatOrNull() ?: 0f,
                                        dietType = dietType,
                                        knownConditions = knownConditions.trim(),
                                        currentSymptoms = currentSymptoms.trim(),
                                        menstrualHistory = if (gender == "Female") menstrualHistory else "",
                                        previousAnemia = previousAnemia,
                                        currentMedications = currentMedications.trim()
                                    )
                                )
                            },
                            enabled = isFormValid,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Proceed", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Icon(icon, null, tint = CrimsonPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CrimsonPrimary
        )
    }
}

/**
 * Small dropdown chip selector for compact selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
