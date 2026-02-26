package com.meditech.hemav.feature.doctor.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import com.meditech.hemav.ui.components.*
import com.meditech.hemav.ui.theme.*

data class DoctorDashboardUiState(
    val todayAppointmentsCount: Int,
    val pendingAppointmentsCount: Int,
    val recoveryRate: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    doctorName: String = "Dr. Sameer",
    profilePicUrl: String = "",
    isPro: Boolean = false,
    onNavigateToAppointments: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToPrescriptions: () -> Unit,
    onNavigateToForum: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToLegal: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DoctorDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var showProInfoDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    
    val todayAppointmentsCount by viewModel.todayAppointmentsCount.collectAsState()
    val pendingAppointmentsCount by viewModel.pendingAppointmentsCount.collectAsState()
    val recoveryRate by viewModel.recoveryRate.collectAsState()
    
    // Group ui state
    val uiState = remember(todayAppointmentsCount, pendingAppointmentsCount, recoveryRate) {
        DoctorDashboardUiState(todayAppointmentsCount, pendingAppointmentsCount, recoveryRate)
    }

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(Color(0xFF001A33), Color(0xFF000D1A), Color.Black)
        } else {
            listOf(NeonCyan, Color(0xFF2979FF), Color(0xFFE3F2FD))
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
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().animateEnter(0),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Good Morning,",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    doctorName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // Doctor Avatar
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
                                            coil.compose.AsyncImage(
                                                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                                    .data(profilePicUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Profile",
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                doctorName.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = NeonCyan,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Help Button
                            IconButton(onClick = { showHelpDialog = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = "Help"
                                )
                            }
                            
                            // Dark Mode Toggle
                            val toggleTheme = LocalThemeToggle.current
                            IconButton(onClick = toggleTheme) {
                                Icon(
                                    if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        }
                    }

                    item {
                        // Statistics Card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth().animateEnter(1),
                            shape = RoundedCornerShape(24.dp),
                            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.4f else 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${uiState.todayAppointmentsCount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = NeonCyan)
                                    Text("Today's Appts", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${uiState.pendingAppointmentsCount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = NeonGreen)
                                    Text("Pending Requests", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${uiState.recoveryRate}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = AccentGold)
                                    Text("Health Success", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Quick Actions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp).animateEnter(2)
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
                                    icon = Icons.Default.CalendarMonth,
                                    title = "Appointments",
                                    color = NeonCyan,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToAppointments
                                )
                                DashboardActionCard(
                                    icon = Icons.AutoMirrored.Filled.Chat,
                                    title = "Consultations",
                                    color = NeonGreen,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToMessages
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                DashboardActionCard(
                                    icon = Icons.Default.Description,
                                    title = "Prescribe",
                                    color = AccentGold,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToPrescriptions
                                )
                                DashboardActionCard(
                                    icon = Icons.Default.Forum,
                                    title = "Forum",
                                    color = MedicalCyan,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToForum
                                )
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
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = "Featured", tint = AccentGold, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Become a Featured Doctor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AccentGold)
                                        Text("Get listed at the top of patient searches to grow your practice.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                        Text("Featured Doctor Benefits", fontWeight = FontWeight.Bold)
                                    }
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Boost your practice with the HemaV Featured Program (₹1999/yr):", style = MaterialTheme.typography.bodyMedium)
                                        
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Top Search Placement\nAppear first when patients search for specialists.", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(verticalAlignment = Alignment.Top) {
                                            Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Unlimited Video Consultations\nNo limits on your digital follow-ups.", style = MaterialTheme.typography.bodySmall)
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
                                isDoctor = true,
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
