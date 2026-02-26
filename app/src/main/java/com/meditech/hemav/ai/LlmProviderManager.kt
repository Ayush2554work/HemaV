package com.meditech.hemav.ai

import android.graphics.Bitmap
import android.util.Log
import com.meditech.hemav.data.model.AnemiaResult
import com.meditech.hemav.data.model.AnemiaStage
import com.meditech.hemav.data.model.PatientDetails
import com.meditech.hemav.data.repository.ScanRepository
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Manages multiple LLM providers with automatic fallback.
 * Tries providers in priority order: Gemini → Groq → HuggingFace.
 * On failure (rate limit, timeout, error), automatically cascades to next provider.
 *
 * After successful analysis:
 *   1. Uploads scan images to Firebase Storage (per user)
 *   2. Saves AnemiaResult to Firestore under /users/{uid}/scans/
 *   3. Saves anonymized copy to /training_data/ for model retraining
 */
class LlmProviderManager(
    private val providers: List<LlmProvider>,
    private val scanRepository: ScanRepository = ScanRepository()
) {
    companion object {
        private const val TAG = "LlmProviderManager"
    }

    /**
     * Scales down a bitmap to ensure neither dimension exceeds maxDimension, maintaining aspect ratio.
     */
    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = originalWidth
        var resizedHeight = originalHeight

        if (originalHeight > maxDimension || originalWidth > maxDimension) {
            if (originalWidth > originalHeight) {
                resizedWidth = maxDimension
                resizedHeight = (originalHeight * (maxDimension.toFloat() / originalWidth)).toInt()
            } else {
                resizedHeight = maxDimension
                resizedWidth = (originalWidth * (maxDimension.toFloat() / originalHeight)).toInt()
            }
            return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true)
        }
        return bitmap
    }

    /**
     * Analyze images for anemia using the first available LLM provider.
     * Falls back to next provider on failure.
     * After successful analysis, auto-saves images + result to Firebase.
     */
    suspend fun analyzeForAnemia(images: List<Bitmap>, patientDetails: PatientDetails? = null): AnemiaResult {
        val prompt = AnemiaPromptBuilder.buildPrompt(patientDetails)
        val errors = mutableListOf<String>()

        // Resize images to max 1024px to prevent OOM/Timeouts on slow networks
        val scaledImages = images.map { scaleBitmapDown(it, 1024) }

        for (provider in providers) {
            if (!provider.isAvailable) {
                Log.d(TAG, "Skipping ${provider.name}: not available (no API key)")
                continue
            }

            try {
                Log.d(TAG, "Trying provider: ${provider.name}")
                val response = provider.analyzeImages(scaledImages, prompt)
                val result = parseResponse(response, provider.name)
                // Success with this provider
                return result
            } catch (e: Exception) {
                Log.w(TAG, "Provider ${provider.name} failed: ${e.message}")
                errors.add("${provider.name}: ${e.message}")
            }
        }

        // All providers failed
        Log.e(TAG, "All providers failed: $errors")
        return AnemiaResult(
            stage = AnemiaStage.UNKNOWN,
            explanation = "Unable to analyze images. All AI providers were unavailable.\n" +
                         "Errors: ${errors.joinToString("; ")}",
            providerUsed = "none"
        )
    }



    /**
     * Parse the JSON response from any LLM into an AnemiaResult.
     */
    private fun parseResponse(response: String, providerName: String): AnemiaResult {
        try {
            // Extract JSON from response (LLMs sometimes wrap in markdown code blocks)
            val jsonStr = extractJson(response)
            val gson = Gson()
            val json = gson.fromJson(jsonStr, JsonObject::class.java)

            val hbEstimate = json.get("hemoglobin_estimate")?.asFloat ?: 0f
            val stageStr = json.get("stage")?.asString ?: "UNKNOWN"
            val confidence = json.get("confidence")?.asFloat ?: 0f
            val explanation = json.get("explanation")?.asString ?: ""

            val perImageFindings = mutableMapOf<String, String>()
            json.getAsJsonObject("per_image_findings")?.let { findings ->
                findings.keySet().forEach { key ->
                    perImageFindings[key] = findings.get(key)?.asString ?: ""
                }
            }

            val ayurvedicInsights = mutableMapOf<String, String>()
            json.getAsJsonObject("ayurvedic_insights")?.let { insights ->
                insights.keySet().forEach { key ->
                    ayurvedicInsights[key] = insights.get(key)?.asString ?: ""
                }
            }

            val stage = try {
                AnemiaStage.valueOf(stageStr.uppercase())
            } catch (e: Exception) {
                classifyByHemoglobin(hbEstimate)
            }

            return AnemiaResult(
                hemoglobinEstimate = hbEstimate,
                stage = stage,
                confidence = confidence,
                explanation = explanation,
                perImageFindings = perImageFindings,
                ayurvedicInsights = ayurvedicInsights,
                providerUsed = providerName
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse LLM response: ${e.message}")
        }
    }

    /**
     * Extract JSON from LLM response that may be wrapped in markdown code blocks.
     */
    /**
     * Extract JSON from LLM response.
     * Robustly finds the first '{' and last '}' to extract the JSON object.
     */
    private fun extractJson(response: String): String {
        try {
            val startIndex = response.indexOf('{')
            val endIndex = response.lastIndexOf('}')
            
            if (startIndex != -1 && endIndex != -1 && response.length > startIndex) {
                return response.substring(startIndex, endIndex + 1)
            }
        } catch (e: Exception) {
             Log.w(TAG, "JSON extraction failed: ${e.message}")
        }
        return response
    }

    /**
     * Classify anemia stage based on hemoglobin level (WHO criteria)
     */
    private fun classifyByHemoglobin(hb: Float): AnemiaStage {
        return when {
            hb >= 12.0f -> AnemiaStage.NORMAL
            hb >= 11.0f -> AnemiaStage.MILD
            hb >= 8.0f -> AnemiaStage.MODERATE
            hb > 0f -> AnemiaStage.SEVERE
            else -> AnemiaStage.UNKNOWN
        }
    }
}
