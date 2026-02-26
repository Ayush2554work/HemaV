package com.meditech.hemav.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.meditech.hemav.feature.auth.*
import com.meditech.hemav.ui.components.FloatingBottomNavigation
import com.meditech.hemav.ui.components.NavigationItem
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.feature.patient.dashboard.PatientDashboardScreen
import com.meditech.hemav.feature.doctor.dashboard.DoctorDashboardScreen
import com.meditech.hemav.feature.patient.anemia.AnemiaScanScreen
import com.meditech.hemav.feature.patient.search.DoctorSearchScreen
import com.meditech.hemav.feature.patient.search.DoctorDetailScreen
import com.meditech.hemav.feature.patient.appointment.BookAppointmentScreen
import com.meditech.hemav.feature.chat.ChatScreen
import com.meditech.hemav.feature.chat.ChatListScreen
import com.meditech.hemav.feature.doctor.prescription.EPrescriptionScreen
import com.meditech.hemav.feature.doctor.appointments.AppointmentManagementScreen
import com.meditech.hemav.feature.doctor.appointments.AppointmentManagementScreen
import com.meditech.hemav.feature.patient.medication.MedicationsScreen
import com.meditech.hemav.feature.patient.reports.ScanHistoryScreen
import com.meditech.hemav.feature.patient.reports.ScanDetailScreen
import com.meditech.hemav.feature.patient.profile.ProfileScreen
import com.meditech.hemav.feature.patient.insights.HealthInsightsScreen
import com.meditech.hemav.feature.patient.insights.HealthInsightsViewModel
import com.meditech.hemav.feature.patient.store.MedicalStoreScreen
import com.meditech.hemav.feature.patient.store.CartScreen
import com.meditech.hemav.feature.patient.store.CartViewModel
import com.meditech.hemav.feature.common.ComingSoonScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.meditech.hemav.data.model.UserRole
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.meditech.hemav.util.RazorpayManager
import com.meditech.hemav.feature.chat.ChatViewModel

@Composable
fun HemaVNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel() // Shared cart state
    val chatViewModel: ChatViewModel = viewModel() // Shared real-time chat state
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userRole = authViewModel.uiState.userRole
    val isDoctor = userRole == UserRole.DOCTOR || currentRoute?.startsWith("doctor") == true || currentRoute == Routes.DOCTOR_CHAT_LIST || currentRoute?.startsWith("create_prescription") == true

    // Fetch real user name from Firestore - re-fetch on login state change
    var realUserName by remember { mutableStateOf("User") }
    var profilePicUrl by remember { mutableStateOf("") }
    var isPro by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(authViewModel.uiState.isLoggedIn) {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        if (uid != null) {
            val userEmail = user.email ?: ""
            isPro = userEmail.equals("ayushkumarwork2554@gmail.com", ignoreCase = true)
            
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users").document(uid).get().await()
                realUserName = doc.getString("name") ?: "User"
                profilePicUrl = doc.getString("profilePicUrl") ?: ""
            } catch (_: Exception) {
                realUserName = user.displayName ?: "User"
                profilePicUrl = user.photoUrl?.toString() ?: ""
            }
        } else {
            realUserName = "User"
            profilePicUrl = ""
            isPro = false
        }
    }

    // Define Navigation Items
    val patientNavItems = listOf(
        NavigationItem(0, Icons.Default.Home, "Home", NeonCyan),
        NavigationItem(1, Icons.Default.CameraAlt, "Scan", NeonRed),
        NavigationItem(2, Icons.AutoMirrored.Filled.Chat, "Chat", NeonGreen),
        NavigationItem(3, Icons.Default.Medication, "Meds", AccentGold)
    )

    val doctorNavItems = listOf(
        NavigationItem(0, Icons.Default.Dashboard, "Home", NeonGreen),
        NavigationItem(1, Icons.Default.CalendarMonth, "Schedule", NeonCyan),
        NavigationItem(2, Icons.AutoMirrored.Filled.Chat, "Chat", AccentGold),
        NavigationItem(3, Icons.Default.Person, "Profile", NeonRed)
    )

    // Determine which menu to show
    val topLevelRoutes = listOf(
        Routes.PATIENT_DASHBOARD,
        Routes.DOCTOR_DASHBOARD,
        Routes.DOCTOR_DASHBOARD,
        // Routes.ANEMIA_SCAN, // Hidden to allow full screen capture
        Routes.SCAN_HISTORY,
        Routes.PATIENT_CHAT_LIST,
        Routes.DOCTOR_CHAT_LIST,
        Routes.MEDICATIONS,
        Routes.PROFILE,
        Routes.APPOINTMENT_REQUESTS,
        Routes.PATIENT_APPOINTMENTS
    )
    
    val showBottomBar = currentRoute in topLevelRoutes
    val currentNavItems = if (isDoctor) doctorNavItems else patientNavItems
    
    // Determine selected item based on route
    val selectedItem = if (isDoctor) {
        when (currentRoute) {
            Routes.DOCTOR_DASHBOARD -> 0
            Routes.APPOINTMENT_REQUESTS -> 1
            Routes.DOCTOR_CHAT_LIST, Routes.CHAT -> 2
            Routes.PROFILE -> 3
            else -> 0
        }
    } else {
        when (currentRoute) {
            Routes.PATIENT_DASHBOARD -> 0
            Routes.ANEMIA_SCAN, Routes.SCAN_HISTORY, Routes.SCAN_DETAIL, Routes.ANEMIA_RESULTS -> 1
            Routes.PATIENT_CHAT_LIST, Routes.CHAT -> 2
            Routes.MEDICATIONS -> 3
            Routes.PATIENT_APPOINTMENTS -> 0 // Keeps home highlighted if navigated from dash
            else -> 0
        }
    }

    // Premium Upgrade Logic Reusable Lambda
    val triggerPremiumUpgrade: () -> Unit = {
        if (context is ComponentActivity) {
            RazorpayManager.startPayment(context, 999) { success, paymentId ->
                if (success) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                FirebaseFirestore.getInstance().collection("users").document(uid).update("isPro", true).await()
                                // Note: we can't easily set isPro locally here since isPro is a var in the outer NavGraph scope driven from ViewModel
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "HemaV Pro Activated! ID: $paymentId", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                } else {
                    Toast.makeText(context, "Payment processing failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Premium Upgrade Logic Reusable Lambda (For Doctors)
    val triggerDoctorPremiumUpgrade: () -> Unit = {
        if (context is ComponentActivity) {
            RazorpayManager.startPayment(context, 1999) { success, paymentId ->
                if (success) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid != null) {
                                FirebaseFirestore.getInstance().collection("doctors").document(uid).update("isPro", true).await()
                                // Note: we can't easily set isPro locally here since isPro is a var in the outer NavGraph scope driven from ViewModel
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Featured Doctor Activated! ID: $paymentId", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                } else {
                    Toast.makeText(context, "Payment processing failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.fillMaxSize()
        ) {
        // ===== AUTH =====
        composable(Routes.SPLASH) {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToDashboard = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // ===== PATIENT DASHBOARD =====
        composable(Routes.PATIENT_DASHBOARD) {
            PatientDashboardScreen(
                userName = realUserName,
                profilePicUrl = profilePicUrl,
                isPro = isPro,
                onNavigateToAnemiaScan = { navController.navigate(Routes.ANEMIA_SCAN) },
                onNavigateToDoctorSearch = { navController.navigate(Routes.DOCTOR_SEARCH) },
                onNavigateToMessages = { navController.navigate(Routes.PATIENT_CHAT_LIST) },
                onNavigateToReports = { navController.navigate(Routes.MY_REPORTS) },
                onNavigateToMedications = { navController.navigate(Routes.MEDICATIONS) },
                onNavigateToAppointments = { navController.navigate(Routes.PATIENT_APPOINTMENTS) },
                onNavigateToInsights = { navController.navigate(Routes.HEALTH_INSIGHTS) },
                onNavigateToForum = { navController.navigate(Routes.FORUM_LIST) },
                onNavigateToStore = { navController.navigate(Routes.MEDICAL_STORE) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToPremium = triggerPremiumUpgrade,
                onNavigateToLegal = { navController.navigate(Routes.LEGAL) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ===== DOCTOR DASHBOARD =====
        composable(Routes.DOCTOR_DASHBOARD) {
            DoctorDashboardScreen(
                doctorName = realUserName,
                profilePicUrl = profilePicUrl,
                isPro = isPro,
                onNavigateToAppointments = { navController.navigate(Routes.APPOINTMENT_REQUESTS) },
                onNavigateToMessages = { navController.navigate(Routes.DOCTOR_CHAT_LIST) },
                onNavigateToPrescriptions = { 
                    Toast.makeText(context, "Select an appointment first to prescribe", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.APPOINTMENT_REQUESTS) 
                },
                onNavigateToForum = { navController.navigate(Routes.FORUM_LIST) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToPremium = triggerDoctorPremiumUpgrade,
                onNavigateToLegal = { navController.navigate(Routes.LEGAL) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ===== ANEMIA SCAN FLOW =====
        composable(Routes.ANEMIA_SCAN) {
            AnemiaScanScreen(
                isPro = isPro,
                onNavigateToPremium = triggerPremiumUpgrade,
                onFindDoctor = { navController.navigate(Routes.DOCTOR_SEARCH) },
                onBack = { navController.popBackStack() }
            )
        }

        // ===== DOCTOR SEARCH & BOOKING =====
        composable(Routes.DOCTOR_SEARCH) {
            DoctorSearchScreen(
                onDoctorSelected = { doctorId ->
                    navController.navigate(Routes.doctorDetail(doctorId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.DOCTOR_DETAIL,
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            DoctorDetailScreen(
                doctorId = doctorId,
                onBookAppointment = { id ->
                    navController.navigate(Routes.bookAppointment(id))
                },
                onStartChat = { id ->
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val chatId = if (currentUid < id) "${currentUid}_$id" else "${id}_$currentUid"
                    navController.navigate(Routes.chat(chatId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.BOOK_APPOINTMENT,
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            BookAppointmentScreen(
                doctorId = doctorId,
                onBookingComplete = {
                    navController.navigate(Routes.PATIENT_DASHBOARD) {
                        popUpTo(Routes.PATIENT_DASHBOARD) { inclusive = true }
                    }
                },
                onStartConsultation = { id ->
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val chatId = if (currentUid < id) "${currentUid}_$id" else "${id}_$currentUid"
                    navController.navigate(Routes.chat(chatId)) {
                        popUpTo(Routes.PATIENT_DASHBOARD)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ===== CHAT =====
        composable(Routes.PATIENT_CHAT_LIST) {
            ChatListScreen(
                isDoctor = false,
                chatViewModel = chatViewModel,
                onChatSelected = { chatId -> navController.navigate(Routes.chat(chatId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DOCTOR_CHAT_LIST) {
            ChatListScreen(
                isDoctor = true,
                chatViewModel = chatViewModel,
                onChatSelected = { chatId -> navController.navigate(Routes.chat(chatId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.CHAT,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() },
                onCallClicked = { type -> 
                    // Use Jitsi Meet for Video/Audio calls
                    val roomName = "HemaV_Consultation_$chatId"
                    val displayName = if (isDoctor) "Doctor" else "Patient"
                    com.meditech.hemav.util.VideoCallManager.startCall(
                        context = context,
                        roomName = roomName,
                        displayName = displayName
                    )
                }
            )
        }

        // ===== APPOINTMENTS =====
        composable(Routes.APPOINTMENT_REQUESTS) {
            AppointmentManagementScreen(
                isDoctor = true, // TODO: detect from auth
                isPro = isPro,
                onNavigateToPremium = triggerDoctorPremiumUpgrade,
                onBack = { navController.popBackStack() },
                onNavigateToPrescription = { patientId, patientName ->
                    navController.navigate(Routes.createPrescription(patientId, patientName))
                }
            )
        }

        composable(Routes.PATIENT_APPOINTMENTS) {
            com.meditech.hemav.feature.doctor.appointments.AppointmentManagementScreen(
                isDoctor = false,
                onBack = { navController.popBackStack() },
                onNavigateToPrescription = { _, _ -> }
            )
        }

        // ===== E-PRESCRIPTION =====
        composable(
            route = Routes.CREATE_PRESCRIPTION,
            arguments = listOf(
                navArgument("patientId") { type = NavType.StringType },
                navArgument("patientName") { type = NavType.StringType; defaultValue = "Patient" }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId") ?: ""
            val patientName = backStackEntry.arguments?.getString("patientName") ?: "Patient"
            EPrescriptionScreen(
                patientId = patientId,
                patientName = patientName,
                onSave = {
                    navController.navigate(Routes.DOCTOR_DASHBOARD) {
                        popUpTo(Routes.DOCTOR_DASHBOARD) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ===== LEGAL =====
        composable(Routes.LEGAL) {
            com.meditech.hemav.feature.common.LegalScreen(
                isDoctor = isDoctor,
                isPro = isPro,
                onBack = { navController.popBackStack() }
            )
        }

        // ===== FORUM =====
        composable(Routes.FORUM_LIST) {
            val forumViewModel: com.meditech.hemav.feature.forum.ForumViewModel = viewModel()
            com.meditech.hemav.feature.forum.ForumListScreen(
                isDoctor = isDoctor,
                viewModel = forumViewModel,
                onPostSelected = { postId -> navController.navigate(Routes.forumDetail(postId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.FORUM_DETAIL,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            val forumViewModel: com.meditech.hemav.feature.forum.ForumViewModel = viewModel()
            com.meditech.hemav.feature.forum.ForumDetailScreen(
                postId = postId,
                isDoctor = isDoctor,
                viewModel = forumViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ===== PLACEHOLDER SCREENS =====
        // ===== HISTORY & REPORTS =====
        composable(Routes.MY_REPORTS) { // Keeping MY_REPORTS route name but showing History
            // Or better, redirect or use ScanHistoryScreen directly
            ScanHistoryScreen(
                onScanSelected = { scanId ->
                    navController.navigate(Routes.scanDetail(scanId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.SCAN_DETAIL,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
            ScanDetailScreen(
                scanId = scanId,
                onFindDoctor = { navController.navigate(Routes.DOCTOR_SEARCH) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MEDICATIONS) {
            MedicationsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                isPro = isPro,
                isDoctor = isDoctor,
                onNavigateToPremium = if (isDoctor) triggerDoctorPremiumUpgrade else triggerPremiumUpgrade,
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable(Routes.HEALTH_INSIGHTS) {
            HealthInsightsScreen(
                isPro = isPro,
                onNavigateToPremium = triggerPremiumUpgrade,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MEDICAL_STORE) {
            MedicalStoreScreen(
                onBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Routes.CART) },
                cartViewModel = cartViewModel
            )
        }

        composable(Routes.CART) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { 
                    if (context is ComponentActivity) {
                        val amount = cartViewModel.getTotalPrice().toInt().coerceAtLeast(1)
                        RazorpayManager.startPayment(context, amount) { success, paymentId ->
                            if (success) {
                                scope.launch(Dispatchers.Main) {
                                    Toast.makeText(context, "Order Placed successfully! ID: $paymentId", Toast.LENGTH_LONG).show()
                                    cartViewModel.clearCart()
                                    navController.popBackStack()
                                }
                            } else {
                                Toast.makeText(context, "Payment failed/cancelled.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                viewModel = cartViewModel
            )
        }

        composable(
            route = Routes.COMING_SOON,
            arguments = listOf(navArgument("featureName") { type = NavType.StringType })
        ) { backStackEntry ->
            val featureName = backStackEntry.arguments?.getString("featureName") ?: "New Feature"
            ComingSoonScreen(
                featureName = featureName,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DOCTOR_PROFILE_SETUP) {
            PlaceholderScreen("Doctor Profile Setup", "KYC & setup coming soon") {
                navController.popBackStack()
            }
        }
    }

    if (showBottomBar) {
        FloatingBottomNavigation(
            items = currentNavItems,
            selectedItem = selectedItem,
            onItemSelected = { index ->
                val targetRoute = if (isDoctor) {
                    when (index) {
                        0 -> Routes.DOCTOR_DASHBOARD
                        1 -> Routes.APPOINTMENT_REQUESTS
                        2 -> Routes.DOCTOR_CHAT_LIST
                        3 -> Routes.PROFILE
                        else -> Routes.DOCTOR_DASHBOARD
                    }
                } else {
                    when (index) {
                        0 -> Routes.PATIENT_DASHBOARD
                        1 -> Routes.ANEMIA_SCAN
                        2 -> Routes.PATIENT_CHAT_LIST
                        3 -> Routes.MEDICATIONS
                        else -> Routes.PATIENT_DASHBOARD
                    }
                }

                // Avoid reloading the same screen
                if (currentRoute != targetRoute) {
                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    message: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Construction,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
