package com.meditech.hemav.feature.patient.search

import androidx.compose.foundation.background
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
import com.meditech.hemav.data.model.DoctorProfile
import com.meditech.hemav.data.repository.DoctorRepository
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailScreen(
    doctorId: String,
    onBookAppointment: (String) -> Unit,
    onStartChat: (String) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { DoctorRepository() }
    var doctor by remember { mutableStateOf<DoctorProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(doctorId) {
        isLoading = true
        // Try Firestore first
        val realDoc = repository.getDoctorProfile(doctorId)
        doctor = realDoc ?: repository.getDemoDoctors().find { it.uid == doctorId }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize().liquidBackground(LiquidGradientPrimary, alpha=0.05f), contentAlignment = Alignment.Center) {
            HemaVLoader()
        }
        return
    }

    if (doctor == null) {
        Box(Modifier.fillMaxSize().liquidBackground(LiquidGradientPrimary, alpha=0.05f), contentAlignment = Alignment.Center) {
            Text("Doctor profile not found", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    val currentDoctor = doctor!! // Safe after null check

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, 
        topBar = {
            TopAppBar(
                title = { Text("Doctor Profile", fontWeight = FontWeight.SemiBold) },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        // Animated Liquid Background
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(NeonCyan, Color(0xFF003300), NeonGreen)
        } else {
            listOf(NeonCyan, Color(0xFF2979FF), NeonGreen)
        }
        
        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = liquidColors
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Profile Image with Clay Effect
                ClayCard(
                    modifier = Modifier.size(120.dp).animateEnter(0),
                    shape = CircleShape,
                    elevation = 12.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(NeonGreen.copy(alpha=0.1f), NeonCyan.copy(alpha=0.1f)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = NeonCyan,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name & Verification
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.animateEnter(1)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Dr. ${currentDoctor.name.replace("Dr.", "").trim()}", 
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (currentDoctor.isVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Verified,
                                "Verified",
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Text(
                        currentDoctor.qualifications,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Row (Glass Cards)
                Row(
                    modifier = Modifier.fillMaxWidth().animateEnter(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassStatCard(
                        modifier = Modifier.weight(1f),
                        value = "${currentDoctor.rating}",
                        label = "Rating",
                        icon = Icons.Default.Star,
                        color = AccentGold
                    )
                    GlassStatCard(
                        modifier = Modifier.weight(1f),
                        value = "${currentDoctor.experience}y",
                        label = "Exp",
                        icon = Icons.Default.WorkHistory,
                        color = NeonCyan
                    )
                    GlassStatCard(
                        modifier = Modifier.weight(1f),
                        value = "${currentDoctor.totalRatings}",
                        label = "Reviews",
                        icon = Icons.Default.Reviews,
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // About Section (Glass)
                GlassCard(
                    modifier = Modifier.fillMaxWidth().animateEnter(3)
                ) {
                    Column {
                        Text(
                            "About",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            currentDoctor.about,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Specialties (Glass)
                GlassCard(modifier = Modifier.fillMaxWidth().animateEnter(4)) {
                    Column {
                        Text(
                            "Specialties",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentDoctor.specialties.forEach { specialty ->
                                Surface(
                                    color = NeonCyan.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(50),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocalHospital,
                                            null,
                                            modifier = Modifier.size(14.dp),
                                            tint = NeonCyan
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            specialty,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Availability
                GlassCard(modifier = Modifier.fillMaxWidth().animateEnter(5)) {
                    Column {
                        Text(
                            "Next Available Slots",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        currentDoctor.availableSlots.forEach { slot ->
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    null,
                                    modifier = Modifier.size(16.dp),
                                    tint = NeonGreen
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(slot, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Bottom spacing for FAB/Buttons
            }

            // Fixed Bottom Action Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .animateEnter(6, delayPerItem = 100)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.9f), MaterialTheme.colorScheme.background)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Message Button (Glass)
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onStartChat(doctorId) }
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Chat, null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message", fontWeight = FontWeight.Bold, color = NeonCyan)
                        }
                    }

                    // Book Button (Clay + Neon)
                    ClayCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = NeonGreen,
                        elevation = 8.dp,
                        onClick = { onBookAppointment(doctorId) }
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Book Now", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
