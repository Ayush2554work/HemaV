package com.meditech.hemav.feature.patient.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.meditech.hemav.R
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.*
import com.meditech.hemav.data.repository.ContentRepository
import com.meditech.hemav.data.repository.ContentType
import com.meditech.hemav.data.repository.WellnessContent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(
    userName: String = "Patient",
    profilePicUrl: String = "",
    isPro: Boolean = false,
    onNavigateToAnemiaScan: () -> Unit,
    onNavigateToDoctorSearch: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToForum: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onLogout: () -> Unit,
    viewModel: PatientDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val upcomingAppointment by viewModel.upcomingAppointment.collectAsState()
    val hasActiveAppointment by viewModel.hasActiveAppointment.collectAsState()
    var showProInfoDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(Color(0xFF003300), Color(0xFF001A1A), Color.Black)
        } else {
            listOf(NeonCyan, NeonGreen, Color(0xFF2979FF))
        }

        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = liquidColors
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 130.dp)
                ) {
                    // Brand Logo Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.hemav_logo_full),
                                contentDescription = "HemaV Logo",
                                modifier = Modifier.height(180.dp),
                                contentScale = ContentScale.Fit,
                                colorFilter = if (isDark) androidx.compose.ui.graphics.ColorFilter.tint(Color.White) else null
                            )
                        }
                    }

                    // Calendar Strip
                    item {
                        HemaVCalendarStrip(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .animateEnter(0)
                        )
                    }

                    // Welcome Header
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateEnter(0),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Welcome back,",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        userName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val toggleTheme = LocalThemeToggle.current
                                    val toggleColor = if(isDark) Color.White else OnSurfaceLight
                                    IconButton(onClick = { showHelpDialog = true }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = "Help",
                                            tint = toggleColor
                                        )
                                    }
                                    IconButton(onClick = toggleTheme) {
                                        Icon(
                                            if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = "Toggle Theme",
                                            tint = toggleColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    if (isPro) {
                                        IconButton(onClick = { showProInfoDialog = true }) {
                                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro Info", tint = AccentGold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    IconButton(onClick = onNavigateToProfile) {
                                        ClayCard(
                                            modifier = Modifier.size(48.dp),
                                            shape = CircleShape,
                                            elevation = 4.dp,
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            onClick = onNavigateToProfile
                                        ) {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                if (!profilePicUrl.isNullOrBlank()) {
                                                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                                                        coil.compose.AsyncImage(
                                                            model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                                .data(profilePicUrl)
                                                                .crossfade(true)
                                                                .build(),
                                                            contentDescription = "Profile",
                                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        userName.take(1).uppercase(),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        color = NeonCyan,
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

                    // Upcoming Appointment Card
                    upcomingAppointment?.let { appointment ->
                        item {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateEnter(1),
                                shape = RoundedCornerShape(24.dp),
                                onClick = onNavigateToAppointments,
                                    backgroundColor = AyurvedicGreen.copy(alpha = if (isDark) 0.08f else 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(AyurvedicGreen.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.EventRepeat,
                                            null,
                                            tint = AyurvedicGreen,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Your Next Appointment",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AyurvedicGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            appointment.doctorName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "${appointment.date} • ${appointment.time}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(onClick = onNavigateToAppointments) {
                                        Icon(Icons.Default.ChevronRight, null, tint = AyurvedicGreen)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // Hero Card - Anemia Scan CTA (Glassy AI Red)
                    item {
                        GlassCard(
                            onClick = onNavigateToAnemiaScan,
                            modifier = Modifier.fillMaxWidth().animateEnter(1),
                            shape = RoundedCornerShape(32.dp),
                            backgroundColor = NeonRed.copy(alpha = if (isDark) 0.15f else 0.12f),
                            borderColor = NeonRed.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp), 
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = NeonRed
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "AI Anemia Vision",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Screening Level",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        "Instant AI-powered hemoglobin check.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = onNavigateToAnemiaScan,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonRed,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Text("Start Scan", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Bloodtype,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(8.dp)
                                            .pulseEffect(),
                                        tint = NeonRed.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // Health Insights Card
                    item {
                        GlassCard(
                            onClick = onNavigateToInsights,
                            modifier = Modifier.fillMaxWidth().animateEnter(2),
                            shape = RoundedCornerShape(26.dp),
                            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MedicalCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QueryStats, null, tint = MedicalCyan)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Health Insights",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Track your hemoglobin trends & AI analysis history",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = MedicalCyan)
                            }
                        }
                    }

                    // Action Grid Title
                    item {
                        Text(
                            "Wellness Services",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 16.dp).animateEnter(2)
                        )
                    }

                    // Action Grid
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.animateEnter(3)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DashboardActionCard(
                                    icon = Icons.Default.Storefront,
                                    title = "Ayurvedic Store",
                                    color = NeonGreen,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToStore
                                )
                                DashboardActionCard(
                                    icon = Icons.Default.PersonSearch,
                                    title = "Find Doctor",
                                    color = NeonCyan,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToDoctorSearch
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DashboardActionCard(
                                    icon = Icons.Default.CalendarMonth,
                                    title = "Appointments",
                                    color = AccentGold,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToAppointments
                                )
                                DashboardActionCard(
                                    icon = Icons.AutoMirrored.Filled.Chat,
                                    title = "Consultations",
                                    color = NeonCyan,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (hasActiveAppointment) {
                                            if (isPro) onNavigateToMessages() else showProInfoDialog = true
                                        } else {
                                            android.widget.Toast.makeText(context, "Please book a consultation first to message doctors.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DashboardActionCard(
                                    icon = Icons.Default.Description,
                                    title = "Reports",
                                    color = NeonRed,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToReports
                                )
                                DashboardActionCard(
                                    icon = Icons.Default.Analytics,
                                    title = "Insights",
                                    color = AccentGold,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (isPro) onNavigateToInsights() else showProInfoDialog = true
                                    }
                                )
                                DashboardActionCard(
                                    icon = Icons.Default.Medication,
                                    title = "Pharmacy",
                                    color = NeonGreen,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (isPro) onNavigateToMedications() else showProInfoDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // Wellness Content Section
                    item {
                        WellnessContentCarousel()
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // Community Forum Card
                    item {
                        GlassCard(
                            onClick = onNavigateToForum,
                            modifier = Modifier.fillMaxWidth().animateEnter(5),
                            shape = RoundedCornerShape(26.dp),
                            backgroundColor = CrimsonPrimary.copy(alpha = if (isDark) 0.1f else 0.05f)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CrimsonPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Forum, null, tint = CrimsonPrimary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Community Forum",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Discuss symptoms and share experiences",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = CrimsonPrimary)
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // HemaV Pro Banner
                    if (!isPro) {
                        item {
                            com.meditech.hemav.ui.components.GlassCard(
                                modifier = Modifier.fillMaxWidth().animateEnter(6),
                                shape = RoundedCornerShape(24.dp),
                                backgroundColor = AccentGold.copy(alpha = if (isDark) 0.15f else 0.1f),
                                borderColor = AccentGold.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showProInfoDialog = true }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro", tint = AccentGold, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Upgrade to HemaV Pro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentGold)
                                        Text("Unlimited AI scans, detailed PDF reports & priority support.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AccentGold)
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }


                     // Pro Info Dialog
                    if (showProInfoDialog) {
                        item {
                            AlertDialog(
                                onDismissRequest = { showProInfoDialog = false },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WorkspacePremium, null, tint = AccentGold, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("HemaV Pro Benefits", fontWeight = FontWeight.Bold)
                                    }
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Unlock the full potential of your health journey with HemaV Pro (₹999/year):", style = MaterialTheme.typography.bodyMedium)
                                        
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Unlimited Consultations\n(Free tier is limited to 3 per month)", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Downloadable PDF Reports\nSave and share your AI Anemia results.", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Full Ayurvedic Pharmacy Access\nOrder proprietary medicines easily.", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Notifications & Treatment Trackers\nNever miss a dose or scan.", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                },
                                confirmButton = {
                                    if (!isPro) {
                                        Button(
                                            onClick = { 
                                                showProInfoDialog = false
                                                onNavigateToPremium()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.White)
                                        ) {
                                            Text("Upgrade Now")
                                        }
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showProInfoDialog = false }) {
                                        Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(24.dp)
                            )
                        }
                    }

                    if (showHelpDialog) {
                        item {
                            GlobalHelpDialog(
                                isPro = isPro,
                                isDoctor = false,
                                onDismiss = { showHelpDialog = false },
                                onUpgrade = { 
                                    showHelpDialog = false
                                    onNavigateToPremium()
                                }
                            )
                        }
                    }

                    // Terms and Conditions Link
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TextButton(onClick = onNavigateToLegal) {
                                Text(
                                    "Terms & Conditions | Privacy Policy",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
fun WellnessContentCarousel() {
    val repository = remember { ContentRepository() }
    val content = remember { repository.getAllContent() }
    
    Column(modifier = Modifier.padding(top = 24.dp).animateEnter(5)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ayurvedic Daily",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Fresh Tips",
                style = MaterialTheme.typography.labelLarge,
                color = AyurvedicGreen,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(content) { item ->
                WellnessCard(item)
            }
        }
    }
}

@Composable
fun WellnessCard(item: WellnessContent) {
    val isYoga = item.type == ContentType.YOGA
    val isDark = LocalIsDark.current
    
    GlassCard(
        modifier = Modifier.width(300.dp).height(180.dp),
        shape = RoundedCornerShape(28.dp),
        backgroundColor = if (isYoga) {
            NeonCyan.copy(alpha = if (isDark) 0.05f else 0.12f)
        } else {
            AccentGold.copy(alpha = if (isDark) 0.05f else 0.12f)
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isYoga) NeonCyan.copy(alpha = 0.2f) else AccentGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isYoga) Icons.Default.SelfImprovement else Icons.Default.Lightbulb,
                        null,
                        tint = if (isYoga) NeonCyan else AccentGold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isYoga) "Yoga Asana" else "Ayurvedic Tip",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isYoga) NeonCyan else AccentGold,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, null, tint = AyurvedicGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    item.benefit,
                    style = MaterialTheme.typography.labelSmall,
                    color = AyurvedicGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardActionCard(
    icon: ImageVector,
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ClayCard(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
                    .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    modifier = Modifier.size(28.dp),
                    tint = color
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
