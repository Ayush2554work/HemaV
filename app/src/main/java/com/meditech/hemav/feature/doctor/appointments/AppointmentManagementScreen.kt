package com.meditech.hemav.feature.doctor.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meditech.hemav.data.model.AppointmentStatus
import com.meditech.hemav.data.model.AppointmentType
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.data.model.Appointment
import com.meditech.hemav.ui.components.HemaVLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentManagementScreen(
    isDoctor: Boolean = true,
    isPro: Boolean = false,
    onNavigateToPremium: () -> Unit = {},
    onBack: () -> Unit,
    onNavigateToPrescription: (String, String) -> Unit = { _, _ -> },
    viewModel: AppointmentViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val tabs = listOf("Upcoming", "Pending", "Completed")
    var selectedTab by remember { 
        mutableIntStateOf(if (!isDoctor) tabs.indexOf("Pending").coerceAtLeast(0) else 0) 
    }

    // Load appointments on start
    LaunchedEffect(Unit) {
        viewModel.loadAppointments(isDoctor)
    }

    val uiState by viewModel.uiState.collectAsState()
    val updatingId by viewModel.updatingAppointmentId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isDoctor) "Manage Appointments" else "My Appointments") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab bar
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (val state = uiState) {
                is AppointmentUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        HemaVLoader()
                    }
                }
                is AppointmentUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is AppointmentUiState.Success -> {
                    val filteredAppointments = remember(state.appointments, selectedTab) {
                        when (selectedTab) {
                            0 -> state.appointments.filter { it.status == AppointmentStatus.CONFIRMED }
                            1 -> state.appointments.filter { it.status == AppointmentStatus.PENDING }
                            2 -> state.appointments.filter { it.status == AppointmentStatus.COMPLETED }
                            else -> state.appointments
                        }
                    }

                    if (filteredAppointments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No ${tabs[selectedTab].lowercase()} appointments", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(bottom = 150.dp),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredAppointments) { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    isDoctor = isDoctor,
                                    isUpdating = updatingId == appointment.id,
                                    onUpdateStatus = { status -> viewModel.updateStatus(appointment.id, status) },
                                    onPrescribe = { onNavigateToPrescription(appointment.patientId, appointment.patientName) }
                                )
                            }
                            
                            // Featured Doctor CTA
                            if (isDoctor && !isPro) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    com.meditech.hemav.ui.components.GlassCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        backgroundColor = AccentGold.copy(alpha = 0.1f),
                                        borderColor = AccentGold.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onNavigateToPremium() }
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro", tint = AccentGold, modifier = Modifier.size(36.dp))
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Get More Patients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentGold)
                                                Text("Upgrade to Featured Doctor to appear at the top of patient searches.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCard(
    appointment: com.meditech.hemav.data.model.Appointment,
    isDoctor: Boolean,
    isUpdating: Boolean,
    onUpdateStatus: (AppointmentStatus) -> Unit,
    onPrescribe: () -> Unit
) {
    var showActions by remember { mutableStateOf(false) }
    val statusColor = when (appointment.status) {
        AppointmentStatus.PENDING -> AccentGold
        AppointmentStatus.CONFIRMED -> AyurvedicGreen
        AppointmentStatus.COMPLETED -> MedicalCyan
        AppointmentStatus.CANCELLED -> SevereRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (appointment.type == com.meditech.hemav.data.model.AppointmentType.VIDEO) Icons.Default.Videocam
                        else Icons.Default.LocalHospital,
                        null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(appointment.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    if (isDoctor && appointment.patientAge.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "${appointment.patientGender}, ${appointment.patientAge}y",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedicalCyan,
                                fontWeight = FontWeight.Bold
                            )
                            if (appointment.patientBloodGroup.isNotEmpty()) {
                                Text(
                                    "BG: ${appointment.patientBloodGroup}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrimsonPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (appointment.patientWeight.isNotEmpty()) {
                                Text(
                                    "${appointment.patientWeight}kg",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AyurvedicGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                AssistChip(
                    onClick = {},
                    label = { Text(appointment.status.name, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = statusColor.copy(alpha = 0.1f), labelColor = statusColor)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                // Formatted date/time
                Text("${appointment.date} at ${appointment.time}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    if (appointment.type == com.meditech.hemav.data.model.AppointmentType.VIDEO) Icons.Default.Videocam else Icons.Default.Place,
                    null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (appointment.type == com.meditech.hemav.data.model.AppointmentType.VIDEO) "Video Call" else "In-Person",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions
            if (isUpdating) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                // Doctor actions for pending appointments
                if (isDoctor && appointment.status == AppointmentStatus.PENDING) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(AppointmentStatus.CANCELLED) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SevereRed)
                        ) { Text("Decline") }

                        Button(
                            onClick = { onUpdateStatus(AppointmentStatus.CONFIRMED) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AyurvedicGreen)
                        ) { Text("Accept") }
                    }
                }
                
                // Doctor actions for confirmed/completed
                if (isDoctor && appointment.status == AppointmentStatus.CONFIRMED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onUpdateStatus(AppointmentStatus.COMPLETED) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalCyan)
                    ) { Text("Mark Completed") }
                }

                if (isDoctor && appointment.status == AppointmentStatus.COMPLETED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onPrescribe,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) { 
                        Icon(Icons.Default.MedicalServices, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Issue Prescription") 
                    }
                }
            }
        }
    }
}
