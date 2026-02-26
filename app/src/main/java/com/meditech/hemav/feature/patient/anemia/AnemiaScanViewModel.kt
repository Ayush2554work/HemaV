package com.meditech.hemav.feature.patient.anemia

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditech.hemav.ai.LlmProviderManager
import com.meditech.hemav.ai.providers.GeminiProvider
import com.meditech.hemav.ai.providers.GroqProvider
import com.meditech.hemav.ai.providers.HuggingFaceProvider
import com.meditech.hemav.data.model.AnemiaResult
import com.meditech.hemav.data.model.AnemiaStage
import com.meditech.hemav.data.model.PatientDetails
import com.meditech.hemav.data.repository.ScanRepository
import kotlinx.coroutines.launch

sealed class ScanUiState {
    object Intro : ScanUiState()
    data class Camera(val stepIndex: Int, val stepName: String, val stepDescription: String) : ScanUiState()
    object Analyzing : ScanUiState()
    data class Result(val result: AnemiaResult) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

class AnemiaScanViewModel : ViewModel() {

    // Simple manual dependency injection for POC
    private val llmManager = LlmProviderManager(
        providers = listOf(
            GeminiProvider(),
            GroqProvider(),
            HuggingFaceProvider()
        )
    )
    private val scanRepository = ScanRepository()

    var uiState by mutableStateOf<ScanUiState>(ScanUiState.Intro)
        private set

    private val _capturedImages = mutableListOf<Bitmap>()
    val capturedImages: List<Bitmap> get() = _capturedImages.toList()

    /** Patient details collected before scanning */
    private var _patientDetails: PatientDetails? = null
    val patientDetails: PatientDetails? get() = _patientDetails
    
    val steps = listOf(
        "Face Photo" to "Clear frontal face photo in natural light",
        "Tongue Photo" to "Open mouth, show full tongue",
        "Lower Eyelid" to "Pull down lower eyelid to show conjunctiva",
        "Palm / Wrist" to "Open palm showing inner wrist and creases",
        "Fingernail Beds" to "Press briefly then release—show nail color"
    )

    /** Store patient details before starting scan or upload */
    fun setPatientDetails(details: PatientDetails) {
        _patientDetails = details
    }

    fun startScanning() {
        _capturedImages.clear()
        uiState = ScanUiState.Camera(
            stepIndex = 1,
            stepName = steps[0].first,
            stepDescription = steps[0].second
        )
    }

    fun onImageCaptured(bitmap: Bitmap) {
        _capturedImages.add(bitmap)
        val nextIndex = _capturedImages.size  // 0-based index into steps
        
        if (nextIndex < steps.size) {
            // Move to next step (1-based display)
            uiState = ScanUiState.Camera(
                stepIndex = nextIndex + 1,
                stepName = steps[nextIndex].first,
                stepDescription = steps[nextIndex].second
            )
        } else {
            // All 5 images captured, start analysis
            analyzeImages()
        }
    }

    /**
     * Handle multiple photos uploaded from gallery at once.
     * Accepts 1-5 photos and starts analysis.
     */
    fun onPhotosUploaded(bitmaps: List<Bitmap>) {
        _capturedImages.clear()
        _capturedImages.addAll(bitmaps.take(5))
        analyzeImages()
    }

    private fun analyzeImages() {
        uiState = ScanUiState.Analyzing
        viewModelScope.launch {
            try {
                // 1. Get AI Analysis (with patient details for better accuracy)
                val initialResult = llmManager.analyzeForAnemia(_capturedImages, _patientDetails)
                
                // 2. Upload images & Save to Firestore
                val savedResultStart = scanRepository.saveCompleteScan(
                    images = _capturedImages,
                    hemoglobinEstimate = initialResult.hemoglobinEstimate,
                    stage = initialResult.stage,
                    confidence = initialResult.confidence,
                    explanation = initialResult.explanation,
                    recommendations = emptyList(), 
                    perImageFindings = initialResult.perImageFindings,
                    ayurvedicInsights = initialResult.ayurvedicInsights,
                    providerUsed = initialResult.providerUsed,
                    patientDetails = _patientDetails
                )
                
                if (savedResultStart.isSuccess) {
                    uiState = ScanUiState.Result(savedResultStart.getOrThrow())
                } else {
                    // Fallback to showing local result if save fails (offline mode?)
                    uiState = ScanUiState.Result(initialResult)
                }

            } catch (e: Exception) {
                uiState = ScanUiState.Error("Analysis failed: ${e.message}")
            }
        }
    }
    
    fun reset() {
        _capturedImages.clear()
        _patientDetails = null
        uiState = ScanUiState.Intro
    }
}
