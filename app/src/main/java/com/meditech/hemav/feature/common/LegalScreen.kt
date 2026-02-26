package com.meditech.hemav.feature.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meditech.hemav.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    isDoctor: Boolean,
    isPro: Boolean,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .padding(20.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = (if (isDoctor) AyurvedicGreen else CrimsonPrimary).copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Gavel,
                        null,
                        tint = if (isDoctor) AyurvedicGreen else CrimsonPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isDoctor) "Doctor Agreement" else "Patient Agreement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPro) "Premium Tier (V2.0)" else "Standard Tier (V2.0)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content
            LegalSection("1. Introduction") {
                Text(
                    "Welcome to HemaV, an AI-powered Ayurvedic health platform. By using this application, you agree to comply with and be bound by the following terms and conditions of use, which together with our privacy policy govern Meditech AI's relationship with you in relation to this app.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LegalSection("2. Medical Disclaimer") {
                Text(
                    "HemaV provides information based on AI analysis of visual data and Ayurvedic principles. It is NOT a substitute for professional medical advice, diagnosis, or treatment. Always seek the advice of your physician or other qualified health provider with any questions you may have regarding a medical condition.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SevereRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isDoctor) {
                DoctorLegalContent(isPro)
            } else {
                PatientLegalContent(isPro)
            }

            LegalSection("3. Data Privacy") {
                Text(
                    "Your health data, including scan images and chat history, is stored securely using Firebase encryption. We do not sell your personal health information to third parties. Anonymized data may be used to improve our AI screening accuracy.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LegalSection("4. Liability") {
                Text(
                    "Meditech AI shall not be liable for any direct, indirect, incidental, or consequential damages resulting from the use or inability to use the HemaV platform or from any information obtained through the app.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                "Last Updated: February 2026",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ColumnScope.PatientLegalContent(isPro: Boolean) {
    LegalSection("Patient Usage Terms") {
        Text(
            if (isPro) {
                "As a HemaV Pro member, you have unlimited access to AI Anemia Screening and personalized Health Insights. You are entitled to priority support and ad-free experience. Consultations booked through the app are subject to the respective doctor's fees and the Razorpay payment gateway terms."
            } else {
                "Standard users are limited to 3 AI Anemia Scans per 24 hours. Basic Health Insights are provided. You may book consultations with Ayurvedic specialists, subject to consultation fees where applicable."
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ColumnScope.DoctorLegalContent(isPro: Boolean) {
    LegalSection("Professional Terms") {
        Text(
            if (isPro) {
                "Featured Doctors are required to maintain a valid medical license and provide accurate Ayurvedic consultations. Your profile will be prioritized in patient searches for one year from the date of activation. Meditech AI facilitates consultations but is not responsible for medical advice provided by practitioners."
            } else {
                "Standard Doctor profiles are eligible to receive appointment requests. You are responsible for managing your availability and providing timely E-Prescriptions via the platform's secure delivery engine."
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun LegalSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
