package com.meditech.hemav.feature.patient.anemia

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditech.hemav.ui.theme.*
import com.meditech.hemav.ui.components.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AnemiaScanScreen(
    isPro: Boolean = false,
    onNavigateToPremium: () -> Unit = {},
    onFindDoctor: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: AnemiaScanViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    // Patient details dialog state
    var showPatientDialog by remember { mutableStateOf(false) }
    // Track what action to do after patient details: "scan" or "upload"
    var pendingAction by remember { mutableStateOf("") }

    // Multi-image gallery picker for upload flow
    val multiImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val bitmaps = uris.mapNotNull { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    bitmap
                } catch (e: Exception) {
                    Log.e("ScanScreen", "Failed to decode image: ${e.message}")
                    null
                }
            }
            if (bitmaps.isNotEmpty()) {
                viewModel.onPhotosUploaded(bitmaps)
            }
        }
    }
    
    // Permission launcher
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Camera State
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Show patient details dialog
    if (showPatientDialog) {
        PatientDetailsDialog(
            onDismiss = {
                showPatientDialog = false
                pendingAction = ""
            },
            onConfirm = { details ->
                viewModel.setPatientDetails(details)
                showPatientDialog = false
                when (pendingAction) {
                    "scan" -> {
                        if (hasCameraPermission) {
                            viewModel.startScanning()
                        } else {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    }
                    "upload" -> {
                        multiImagePicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                }
                pendingAction = ""
            }
        )
    }

    Scaffold { padding ->
        when (val state = uiState) {
            is ScanUiState.Intro -> {
                AnemiaScanIntro(
                    isPro = isPro,
                    onNavigateToPremium = onNavigateToPremium,
                    onStartCapture = {
                        if (hasCameraPermission) {
                            if (viewModel.patientDetails != null) {
                                viewModel.startScanning()
                            } else {
                                pendingAction = "scan"
                                showPatientDialog = true
                            }
                        } else {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onUploadPhotos = {
                        if (viewModel.patientDetails != null) {
                            // Patient details already filled — go straight to upload
                            multiImagePicker.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        } else {
                            pendingAction = "upload"
                            showPatientDialog = true
                        }
                    },
                    onBack = onBack
                )
            }
            is ScanUiState.Camera -> {
                if (hasCameraPermission) {
                    CameraCaptureScreen(
                        stepName = state.stepName,
                        stepDescription = state.stepDescription,
                        stepIndex = state.stepIndex,
                        totalSteps = 5,
                        lensFacing = lensFacing,
                        flashMode = flashMode,
                        onLensChange = { lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK },
                        onFlashChange = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                        },
                        onImageCaptured = { bitmap ->
                            viewModel.onImageCaptured(bitmap)
                        },
                        onBack = { viewModel.reset() }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Camera permission required")
                    }
                }
            }
            is ScanUiState.Analyzing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HemaVLoader(size = 80.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analyzing images with AI...", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            is ScanUiState.Result -> {
                AnemiaResultsScreen(
                    result = state.result,
                    capturedBitmaps = viewModel.capturedImages,
                    isPro = isPro,
                    onFindDoctor = onFindDoctor,
                    onBack = { 
                        viewModel.reset()
                        onBack() 
                    }
                )
            }
            is ScanUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, null, tint = ErrorColor, modifier = Modifier.size(48.dp))
                        Text("Error: ${state.message}")
                        Button(onClick = { viewModel.reset() }) { Text("Try Again") }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraCaptureScreen(
    stepName: String,
    stepDescription: String,
    stepIndex: Int,
    totalSteps: Int,
    lensFacing: Int,
    flashMode: Int,
    onLensChange: () -> Unit,
    onFlashChange: () -> Unit,
    onImageCaptured: (Bitmap) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Gallery photo picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    onImageCaptured(bitmap)
                }
            } catch (e: Exception) {
                Log.e("CameraCapture", "Failed to load gallery image: ${e.message}")
            }
        }
    }
    
    // Camera State
    val previewView = remember { PreviewView(context).apply { 
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }}
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // 1. Initialize CameraProvider once
    LaunchedEffect(Unit) {
        val provider = suspendCoroutine<ProcessCameraProvider> { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    continuation.resume(cameraProviderFuture.get())
                } catch (e: Exception) {
                    Log.e("CameraX", "CameraProvider init failed", e)
                    // In a real app, handle error state
                }
            }, ContextCompat.getMainExecutor(context))
        }
        cameraProvider = provider
    }

    // 2. Re-bind camera when Provider, Lens, or Flash changes
    LaunchedEffect(cameraProvider, lensFacing, flashMode) {
        val provider = cameraProvider ?: return@LaunchedEffect

        try {
            provider.unbindAll()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashMode)
                .build()

            // Update state so the shutter button uses the valid use-case
            imageCapture = capture

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture
            )
        } catch (e: Exception) {
            Log.e("CameraX", "Binding failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview - AndroidView just holds the view now
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlays
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(bottom = 50.dp), // Adjust padding for controls
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }

                // Flash Control
                IconButton(
                    onClick = onFlashChange,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                     Icon(
                        imageVector = when(flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        },
                        contentDescription = "Flash",
                        tint = if (flashMode == ImageCapture.FLASH_MODE_OFF) Color.White.copy(alpha=0.6f) else Color.Yellow
                    )
                }
                
                Text(
                    "$stepIndex / $totalSteps",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            // Capture Button & Instructions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stepName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stepDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bottom Controls Row
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.CenterVertically
                ) {
                   // Gallery picker button
                   IconButton(
                       onClick = { galleryLauncher.launch("image/*") },
                       modifier = Modifier
                           .size(48.dp)
                           .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                   ) {
                       Icon(Icons.Default.PhotoLibrary, "Pick from Gallery", tint = Color.White)
                   }

                    // Shutter Button
                    Surface(
                         onClick = {
                            val captureUseCase = imageCapture
                            if (captureUseCase != null) {
                                takePhoto(context, captureUseCase, lensFacing, ContextCompat.getMainExecutor(context)) { bitmap ->
                                    if (bitmap != null) {
                                        onImageCaptured(bitmap)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .border(4.dp, Color.White, CircleShape),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    // Switch Camera
                    IconButton(
                        onClick = onLensChange,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Cameraswitch, "Switch Camera", tint = Color.White)
                    }
                }
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    lensFacing: Int,
    executor: Executor,
    onImageCaptured: (Bitmap?) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "scan_${System.currentTimeMillis()}.jpg"
    )

    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            
            val rotation = image.imageInfo.rotationDegrees
            if (rotation != 0) {
                 val matrix = Matrix()
                 matrix.postRotate(rotation.toFloat())
                 if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                     matrix.postScale(-1f, 1f)
                 }
                 bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                 if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                     val matrix = Matrix()
                     matrix.postScale(-1f, 1f)
                     bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                 }
            }

            onImageCaptured(bitmap)
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraCapture", "Photo capture failed: ${exception.message}", exception)
            onImageCaptured(null)
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnemiaScanIntro(
    isPro: Boolean,
    onNavigateToPremium: () -> Unit,
    onStartCapture: () -> Unit,
    onUploadPhotos: () -> Unit = {},
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Anemia Screening") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        // Animated Liquid Background
        AnimatedLiquidBackground(
            modifier = Modifier.padding(padding),
            colors = listOf(NeonRed, Color(0xFFFF5252), Color(0xFFD50000)), // Red for blood/scan
            speedMultiplier = 1.2f // Faster for scanning activity
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 150.dp), // Safe zone for nav bar
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(CrimsonPrimary, CrimsonLight)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            "Non-Invasive Screening",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Take 5 photos for AI-powered anemia detection.\nNo needles, no blood test required.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step list
            Text(
                "5 Required Photos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val steps = listOf(
                Triple(Icons.Default.Face, "Face Photo", "Clear frontal face photo in natural light"),
                Triple(Icons.Default.Visibility, "Tongue Photo", "Open mouth, show full tongue"),
                Triple(Icons.Default.RemoveRedEye, "Lower Eyelid", "Pull down lower eyelid to show conjunctiva"),
                Triple(Icons.Default.PanTool, "Palm / Wrist", "Open palm showing inner wrist and creases"),
                Triple(Icons.Default.Fingerprint, "Fingernail Beds", "Press briefly then release—show nail color")
            )

            steps.forEachIndexed { index, (icon, title, desc) ->
                ScanStepItem(
                    stepNumber = index + 1,
                    icon = icon,
                    title = title,
                    description = desc
                )
                if (index < steps.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pro Prompt
            if (!isPro) {
                com.meditech.hemav.ui.components.GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = AccentGold.copy(alpha = 0.15f),
                    borderColor = AccentGold.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPremium() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = "Pro", tint = AccentGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Skip the AI Queue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AccentGold)
                            Text("Pro members get instant priority scans.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha=0.9f))
                        }
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = AccentGold, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Start Button
            Button(
                onClick = onStartCapture,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
            ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Start Scanning",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Upload Photos Button
            OutlinedButton(
                onClick = onUploadPhotos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, CrimsonPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CrimsonPrimary
                )
            ) {
                Icon(Icons.Default.PhotoLibrary, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Upload Photos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            }
        }
    }
}

@Composable
fun ScanStepItem(
    stepNumber: Int,
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step number badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CrimsonPrimary, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$stepNumber",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                icon, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

