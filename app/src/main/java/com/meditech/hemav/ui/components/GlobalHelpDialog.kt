package com.meditech.hemav.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.meditech.hemav.ui.theme.*

@Composable
fun GlobalHelpDialog(
    isPro: Boolean,
    isDoctor: Boolean,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (isPro) AccentGold.copy(alpha = 0.15f) 
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPro) Icons.Default.Stars else Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isPro) AccentGold else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isDoctor) "Doctor Support" else "Patient Support",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (isPro) "Premium Priority Support" else "Standard Account Help",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPro) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Content based on Tiers
                if (isDoctor) {
                    if (isPro) {
                        DoctorProHelpContent()
                    } else {
                        DoctorFreeHelpContent(onUpgrade)
                    }
                } else {
                    if (isPro) {
                        PatientProHelpContent()
                    } else {
                        PatientFreeHelpContent(onUpgrade)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPro) AccentGold else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isPro) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HelpItem(icon: ImageVector, title: String, description: String, iconColor: Color = NeonCyan) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = iconColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PatientFreeHelpContent(onUpgrade: () -> Unit) {
    Column {
        HelpItem(
            Icons.Default.Camera, 
            "Anemia Screening", 
            "Take 5 clear photos of eyes, nails, and tongue for AI analysis."
        )
        HelpItem(
            Icons.Default.Update, 
            "Daily Limits", 
            "Standard accounts are limited to 1 AI scan per 24 hours."
        )
        HelpItem(
            Icons.Default.Star, 
            "HemaV Pro", 
            "Upgrade for unlimited scans and priority doctor consultations."
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ClayCard(
            onClick = onUpgrade,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            color = AccentGold,
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Upgrade to Pro", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun PatientProHelpContent() {
    Column {
        HelpItem(
            Icons.Default.VerifiedUser, 
            "Unlimited Access", 
            "Your account has permanent unlimited AI scans and PDF reports."
        )
        HelpItem(
            Icons.Default.SupportAgent, 
            "Priority Support", 
            "Pro members get 24/7 technical support at premium@hemav.in"
        )
        HelpItem(
            Icons.Default.Insights, 
            "Advanced Insights", 
            "Check 'Health Insights' for your personalized Ayurvedic diet plan."
        )
    }
}

@Composable
fun DoctorFreeHelpContent(onUpgrade: () -> Unit) {
    Column {
        HelpItem(
            Icons.Default.Assignment, 
            "Appointments", 
            "Review and confirm patient requests from your dashboard."
        )
        HelpItem(
            Icons.Default.Description, 
            "E-Prescriptions", 
            "Issues digital prescriptions easily from the Consultation screen."
        )
        HelpItem(
            Icons.Default.ShowChart, 
            "Featured Profile", 
            "Upgrade to Pro to appear at the top of patient search results."
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        ClayCard(
            onClick = onUpgrade,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            color = NeonCyan,
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Get Doctor Pro", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun DoctorProHelpContent() {
    Column {
        HelpItem(
            Icons.Default.Visibility, 
            "Maximum Visibility", 
            "Your profile is currently featured in the top results for patients."
        )
        HelpItem(
            Icons.Default.FactCheck, 
            "Patient Tracker", 
            "Advanced treatment tracking is coming soon to your dashboard."
        )
        HelpItem(
            Icons.Default.BusinessCenter, 
            "Concierge Support", 
            "Dedicated support line for medical professionals is now active."
        )
    }
}
