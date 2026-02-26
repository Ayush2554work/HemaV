package com.meditech.hemav.feature.patient.appointment

import androidx.compose.foundation.background
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
import com.meditech.hemav.data.model.AppointmentType
import com.meditech.hemav.data.repository.DoctorRepository
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.*
import com.meditech.hemav.ui.components.HemaVLoader
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.meditech.hemav.util.RazorpayManager
import androidx.activity.ComponentActivity
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.automirrored.filled.*
import java.util.*
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    doctorId: String,
    onBookingComplete: () -> Unit,
    onStartConsultation: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: BookAppointmentViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current as? ComponentActivity
    val repository = remember { DoctorRepository() }
    val doctor = remember { repository.getDemoDoctors().find { it.uid == doctorId } }
    var selectedType by remember { mutableStateOf(AppointmentType.VIDEO) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Patient Details
    var patientAge by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }
    var patientBloodGroup by remember { mutableStateOf("") }
    var patientWeight by remember { mutableStateOf("") }
    
    // Removed local state implies using viewModel state
    // var isBooking by remember { mutableStateOf(false) } 
    // var bookingSuccess by remember { mutableStateOf(false) }

    val dates = remember {
        val list = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.US)
        for (i in 0 until 7) {
            list.add(sdf.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    val times = listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:00 PM", "04:00 PM")

    if (viewModel.bookingSuccess) {
        // Success screen
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = AyurvedicGreen,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Appointment Booked!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "The doctor will confirm your appointment shortly.\nYou'll receive a notification.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Enhanced Success Summary
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, null, tint = MedicalCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$selectedDate at $selectedTime", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (selectedType == AppointmentType.VIDEO) Icons.Default.Videocam else Icons.Default.Place, null, tint = AyurvedicGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (selectedType == AppointmentType.VIDEO) "Video Consultation" else "In-Person Visit", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onStartConsultation(doctorId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalCyan)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Chat")
                    }
                    OutlinedButton(
                        onClick = onBookingComplete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Dashboard")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Appointment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(Color(0xFF003300), Color(0xFF004D40), Color(0xFF001100))
        } else {
             listOf(AyurvedicGreen, MedicalCyan, Color(0xFF80CBC4))
        }

        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = liquidColors
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Glass Container
                GlassCard(
                    modifier = Modifier.fillMaxWidth().animateEnter(0),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column {
                        // Consultation Type
                        Text(
                            "Consultation Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConsultationTypeCard(
                    icon = Icons.Default.Videocam,
                    title = "Video Call",
                    subtitle = "Online consultation",
                    selected = selectedType == AppointmentType.VIDEO,
                    color = MedicalCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = AppointmentType.VIDEO }
                )
                ConsultationTypeCard(
                    icon = Icons.Default.LocalHospital,
                    title = "In-Person",
                    subtitle = "Visit clinic",
                    selected = selectedType == AppointmentType.IN_PERSON,
                    color = AyurvedicGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedType = AppointmentType.IN_PERSON }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date selection
            Text(
                "Select Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dates.take(3).forEach { date ->
                    FilterChip(
                        selected = selectedDate == date,
                        onClick = { selectedDate = date },
                        label = { Text(date, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonPrimary.copy(alpha = 0.12f),
                            selectedLabelColor = CrimsonPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dates.drop(3).forEach { date ->
                    FilterChip(
                        selected = selectedDate == date,
                        onClick = { selectedDate = date },
                        label = { Text(date, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonPrimary.copy(alpha = 0.12f),
                            selectedLabelColor = CrimsonPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f)) // fill remaining space
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time selection
            Text(
                "Select Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Time slots in 2 rows of 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                times.take(3).forEach { time ->
                    FilterChip(
                        selected = selectedTime == time,
                        onClick = { selectedTime = time },
                        label = { Text(time, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedicalCyan.copy(alpha = 0.12f),
                            selectedLabelColor = MedicalCyan
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                times.drop(3).forEach { time ->
                    FilterChip(
                        selected = selectedTime == time,
                        onClick = { selectedTime = time },
                        label = { Text(time, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedicalCyan.copy(alpha = 0.12f),
                            selectedLabelColor = MedicalCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(20.dp))

            // Patient Details Form
            Text(
                "Patient Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = patientAge,
                    onValueChange = { if (it.length <= 3) patientAge = it },
                    label = { Text("Age") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                OutlinedTextField(
                    value = patientWeight,
                    onValueChange = { if (it.length <= 3) patientWeight = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Gender", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Male", "Female", "Other").forEach { gender ->
                    FilterChip(
                        selected = patientGender == gender,
                        onClick = { patientGender = gender },
                        label = { Text(gender) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedicalCyan.copy(alpha = 0.12f),
                            selectedLabelColor = MedicalCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Blood Group", style = MaterialTheme.typography.labelMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val groups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    groups.take(4).forEach { group ->
                        FilterChip(
                            selected = patientBloodGroup == group,
                            onClick = { patientBloodGroup = group },
                            label = { Text(group) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimary.copy(alpha = 0.12f),
                                selectedLabelColor = CrimsonPrimary
                            )
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    groups.drop(4).forEach { group ->
                        FilterChip(
                            selected = patientBloodGroup == group,
                            onClick = { patientBloodGroup = group },
                            label = { Text(group) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimary.copy(alpha = 0.12f),
                                selectedLabelColor = CrimsonPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Symptoms / Reasons") },
                placeholder = { Text("Describe your symptoms or reason for visit...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(14.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary card
            if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Booking Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow("Type", if (selectedType == AppointmentType.VIDEO) "Video Call" else "In-Person")
                        SummaryRow("Date", selectedDate)
                        SummaryRow("Time", selectedTime)
                        SummaryRow("Patient", "$patientGender, $patientAge yrs")
                        doctor?.let {
                            SummaryRow("Fee", "₹${it.consultationFee.toInt()}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Book button
            Button(
                onClick = {
                    val fee = doctor?.consultationFee?.toInt() ?: 0
                    if (fee > 0 && context != null) {
                        RazorpayManager.startPayment(context, fee) { success, paymentId ->
                            if (success) {
                                viewModel.bookAppointment(
                                    context = context,
                                    doctorId = doctorId,
                                    doctorName = doctor?.name ?: "Doctor",
                                    type = selectedType,
                                    dateStr = selectedDate,
                                    timeStr = selectedTime,
                                    notes = notes,
                                    patientAge = patientAge,
                                    patientGender = patientGender,
                                    patientBloodGroup = patientBloodGroup,
                                    patientWeight = patientWeight
                                )
                                Toast.makeText(context, "Payment Successful: $paymentId", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Payment Failed or Cancelled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        viewModel.bookAppointment(
                            context = context ?: return@Button,
                            doctorId = doctorId,
                            doctorName = doctor?.name ?: "Doctor",
                            type = selectedType,
                            dateStr = selectedDate,
                            timeStr = selectedTime,
                            notes = notes,
                            patientAge = patientAge,
                            patientGender = patientGender,
                            patientBloodGroup = patientBloodGroup,
                            patientWeight = patientWeight
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedDate.isNotEmpty() && selectedTime.isNotEmpty() && 
                          patientAge.isNotEmpty() && patientGender.isNotEmpty() && !viewModel.isBooking,
                colors = ButtonDefaults.buttonColors(containerColor = AyurvedicGreen)
            ) {
                if (viewModel.isBooking) {
                    HemaVLoader(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.EventAvailable, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Booking", fontWeight = FontWeight.Bold)
                }
            }

                } // End Glass Card
            }
        }
    }
}
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationTypeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, null,
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
