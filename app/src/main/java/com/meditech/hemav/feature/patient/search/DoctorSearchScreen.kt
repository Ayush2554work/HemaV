package com.meditech.hemav.feature.patient.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meditech.hemav.data.model.DoctorProfile
import com.meditech.hemav.data.repository.DoctorRepository
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.*
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSearchScreen(
    onDoctorSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { DoctorRepository() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecialty by remember { mutableStateOf<String?>(null) }
    var doctors by remember { mutableStateOf<List<DoctorProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var sortBy by remember { mutableStateOf("rating") } // rating, fee, location

    // Fetch real doctors from Firestore, fallback to demo
    LaunchedEffect(Unit) {
        isLoading = true
        val firestoreDoctors = repository.searchDoctors()
        // If we have real doctors in Firestore, use them. Only show demo if Firestore is empty.
        doctors = if (firestoreDoctors.isNotEmpty()) firestoreDoctors else repository.getDemoDoctors()
        isLoading = false
    }

    val specialties = listOf(
        "All", "General Ayurveda", "Panchakarma", "Rasayana",
        "Raktamokshana", "Hematology", "Women's Health",
        "Nadi Pariksha", "Yoga Therapy", "Nutrition"
    )

    val filteredDoctors = remember(searchQuery, selectedSpecialty, doctors, sortBy) {
        doctors.filter { doc ->
            val matchesSearch = searchQuery.isBlank() ||
                doc.name.contains(searchQuery, ignoreCase = true) ||
                doc.qualifications.contains(searchQuery, ignoreCase = true) ||
                doc.about.contains(searchQuery, ignoreCase = true) ||
                doc.city.contains(searchQuery, ignoreCase = true) ||
                doc.specialties.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesSpecialty = selectedSpecialty == null || selectedSpecialty == "All" ||
                doc.specialties.contains(selectedSpecialty)

            matchesSearch && matchesSpecialty
        }.let { list ->
            when (sortBy) {
                "fee" -> list.sortedBy { it.consultationFee }
                "location" -> list.sortedBy { it.city }
                else -> list.sortedByDescending { it.rating }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Find Ayurvedic Doctor", fontWeight = FontWeight.SemiBold) },
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        val isDark = LocalIsDark.current
        val liquidColors = if (isDark) {
            listOf(Color(0xFF003300), Color(0xFF001A1A), Color.Black)
        } else {
             listOf(NeonGreen, NeonCyan, Color(0xFF00E676))
        }

        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = liquidColors
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Search bar (Glass)
                    GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).animateEnter(0),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) Text("Search by specialty...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            androidx.compose.foundation.text.BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Sort chips
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).animateEnter(1),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sort:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
                    listOf("rating" to "⭐ Rating", "fee" to "💰 Fee", "location" to "📍 Location").forEach { (key, label) ->
                        FilterChip(
                            selected = sortBy == key,
                            onClick = { sortBy = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimary.copy(alpha = 0.8f),
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // Specialty chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp).animateEnter(2)
                ) {
                    items(specialties) { specialty ->
                        val isSelected = selectedSpecialty == specialty || (selectedSpecialty == null && specialty == "All")
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedSpecialty = if (specialty == "All") null else specialty
                            },
                            label = { Text(specialty) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonGreen.copy(alpha = 0.8f),
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.6f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if(isSelected) Color.Transparent else NeonGreen.copy(alpha=0.3f)
                            )
                        )
                    }
                }

                Text(
                    "${filteredDoctors.size} doctors found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).animateEnter(3)
                )

                if (isLoading) {
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonGreen)
                    }
                } else {
                // Doctor cards
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredDoctors.size) { index ->
                        val doctor = filteredDoctors[index]
                        DoctorCard(
                            doctor = doctor,
                            onClick = { onDoctorSelected(doctor.uid) },
                            modifier = Modifier.animateEnter(index + 4)
                        )
                    }
                }
                }
            }
        }
    }
}


@Composable
fun DoctorCard(
    doctor: DoctorProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ClayCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        elevation = 8.dp
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                // Doctor avatar with photo support
                val context = LocalContext.current
                if (doctor.profilePicUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(doctor.profilePicUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Doctor Photo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(1.dp, NeonGreen.copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(1.dp, NeonGreen.copy(alpha=0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            doctor.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val displayName = remember(doctor.name) {
                        val nameStr = doctor.name.ifBlank { "Doctor" }
                        if (nameStr.startsWith("Dr.", ignoreCase = true)) nameStr else "Dr. $nameStr"
                    }
                    Text(
                        displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        buildString {
                            if (doctor.degree.isNotBlank()) append("${doctor.degree} • ")
                            append(doctor.qualifications)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = AccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${doctor.rating}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            " (${doctor.totalRatings} verified)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(
                            Icons.Default.WorkHistory,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = NeonCyan
                        )
                        Text(
                            " ${doctor.experience} yrs exp.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Verified badge
                if (doctor.isVerified) {
                    Icon(
                        Icons.Default.Verified,
                        "Verified",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Specialties chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                doctor.specialties.take(3).forEach { specialty ->
                    Surface(
                        color = NeonGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            specialty,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = NeonGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        doctor.city.ifBlank { doctor.clinicAddress.split(",").first() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Show fee with 10% platform markup
                val displayFee = (doctor.consultationFee * 1.10).toInt()
                Text(
                    "₹$displayFee",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }
        }
    }
}
