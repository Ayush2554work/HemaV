package com.meditech.hemav.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.meditech.hemav.data.model.AnemiaResult
import com.meditech.hemav.data.model.AnemiaStage
import com.meditech.hemav.data.model.PatientDetails
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Repository for uploading scan images to Firebase Storage
 * and persisting AI anemia results to Firestore.
 *
 * Data structure in Firebase:
 *
 * Storage: /scans/{userId}/{scanId}/photo_{index}.jpg
 * Firestore: /users/{userId}/scans/{scanId} → AnemiaResult document
 * Firestore: /training_data/{scanId} → anonymized copy for training
 */
class ScanRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    /**
     * Upload all 5 scan images to Firebase Storage under the user's folder.
     * Returns download URL list.
     */
    suspend fun uploadScanImages(
        userId: String,
        scanId: String,
        images: List<Bitmap>
    ): Result<List<String>> {
        return try {
            val downloadUrls = mutableListOf<String>()

            images.forEachIndexed { index, bitmap ->
                val imageName = "photo_${index}.jpg"
                val ref = storage.reference
                    .child("scans")
                    .child(userId)
                    .child(scanId)
                    .child(imageName)

                // Compress bitmap to JPEG
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                val data = baos.toByteArray()

                // Upload
                ref.putBytes(data).await()

                // Get download URL
                val url = ref.downloadUrl.await().toString()
                downloadUrls.add(url)
            }

            Result.success(downloadUrls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save the complete AI analysis result to Firestore.
     * Stores under: /users/{userId}/scans/{scanId}
     * 
     * Uses manual Map serialization to avoid Firestore enum deserialization issues.
     */
    suspend fun saveAnemiaResult(
        userId: String,
        result: AnemiaResult
    ): Result<String> {
        return try {
            val scanDocRef = firestore.collection("users")
                .document(userId)
                .collection("scans")
                .document(result.id.ifBlank { UUID.randomUUID().toString() })

            val docId = scanDocRef.id

            // Manual serialization to avoid Firestore enum issues
            val resultMap = mutableMapOf<String, Any>(
                "id" to docId,
                "patientId" to userId,
                "patientName" to result.patientName,
                "patientAge" to result.patientAge,
                "patientGender" to result.patientGender,
                "patientDetailsJson" to result.patientDetailsJson,
                "hemoglobinEstimate" to result.hemoglobinEstimate,
                "stage" to result.stage.name,  // Store enum as String
                "confidence" to result.confidence,
                "explanation" to result.explanation,
                "perImageFindings" to result.perImageFindings,
                "ayurvedicInsights" to result.ayurvedicInsights,
                "providerUsed" to result.providerUsed,
                "imageUrls" to result.imageUrls,
                "timestamp" to result.timestamp
            )

            scanDocRef.set(resultMap).await()

            // Also save to training_data collection (anonymized)
            saveTrainingData(result.copy(id = docId, patientId = userId))

            Result.success(docId)
        } catch (e: Exception) {
            android.util.Log.e("ScanRepository", "Failed to save result: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Full pipeline: save result to Firestore FIRST, then upload images.
     * This ensures the scan appears in Reports even if image upload fails.
     */
    suspend fun saveCompleteScan(
        images: List<Bitmap>,
        hemoglobinEstimate: Float,
        stage: AnemiaStage,
        confidence: Float,
        explanation: String,
        recommendations: List<String>,
        perImageFindings: Map<String, String>,
        ayurvedicInsights: Map<String, String> = emptyMap(),
        providerUsed: String,
        patientDetails: PatientDetails? = null
    ): Result<AnemiaResult> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated"))

        val scanId = UUID.randomUUID().toString()

        // Step 1: Save result to Firestore FIRST (ensures it appears in Reports)
        val patientDetailsJson = if (patientDetails != null) {
            try { Gson().toJson(patientDetails) } catch (_: Exception) { "" }
        } else ""

        val anemiaResult = AnemiaResult(
            id = scanId,
            patientId = userId,
            patientName = patientDetails?.name ?: "",
            patientAge = patientDetails?.age ?: 0,
            patientGender = patientDetails?.gender ?: "",
            patientDetailsJson = patientDetailsJson,
            hemoglobinEstimate = hemoglobinEstimate,
            stage = stage,
            confidence = confidence,
            explanation = explanation,
            perImageFindings = perImageFindings,
            ayurvedicInsights = ayurvedicInsights,
            providerUsed = providerUsed,
            imageUrls = emptyList(), // Will be updated if upload succeeds
            timestamp = System.currentTimeMillis()
        )

        val saveResult = saveAnemiaResult(userId, anemiaResult)
        if (saveResult.isFailure) {
            android.util.Log.e("ScanRepository", "Firestore save failed: ${saveResult.exceptionOrNull()?.message}")
            return Result.failure(saveResult.exceptionOrNull()!!)
        }

        // Step 2: Upload images (best-effort — scan is already saved)
        try {
            val imageUrlsResult = uploadScanImages(userId, scanId, images)
            if (imageUrlsResult.isSuccess) {
                val imageUrls = imageUrlsResult.getOrThrow()
                // Update the document with image URLs
                firestore.collection("users")
                    .document(userId)
                    .collection("scans")
                    .document(scanId)
                    .update("imageUrls", imageUrls)
                    .await()
            } else {
                android.util.Log.w("ScanRepository", "Image upload failed (non-critical): ${imageUrlsResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            android.util.Log.w("ScanRepository", "Image upload/update failed (non-critical): ${e.message}")
        }

        // Step 3: Update user scan count (best-effort)
        try { updateUserScanStats(userId) } catch (_: Exception) {}

        // Step 4: Sync to backend API (best-effort — Firestore is source of truth for now)
        try {
            withContext(Dispatchers.IO) {
                com.meditech.hemav.data.remote.HemavApiClient.saveScanResult(
                    riskLevel = stage.name,
                    confidence = confidence,
                    hemoglobinEstimate = hemoglobinEstimate.toString(),
                    details = explanation,
                    recommendations = recommendations,
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("ScanRepository", "Backend sync failed (non-critical): ${e.message}")
        }

        return Result.success(anemiaResult)
    }

    /**
     * Get all scan results for a user, ordered by most recent.
     */
    suspend fun getUserScans(userId: String): List<AnemiaResult> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("scans")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    documentToAnemiaResult(doc.data)
                } catch (e: Exception) {
                    android.util.Log.w("ScanRepository", "Failed to parse scan doc ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ScanRepository", "getUserScans failed: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get a specific scan result by ID.
     */
    suspend fun getScanById(userId: String, scanId: String): AnemiaResult? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("scans")
                .document(scanId)
                .get().await()
            documentToAnemiaResult(doc.data)
        } catch (e: Exception) {
            android.util.Log.e("ScanRepository", "getScanById failed: ${e.message}", e)
            null
        }
    }

    /**
     * Manual deserialization from Firestore document to AnemiaResult.
     * Handles enum conversion and missing fields gracefully.
     */
    @Suppress("UNCHECKED_CAST")
    private fun documentToAnemiaResult(data: Map<String, Any>?): AnemiaResult? {
        data ?: return null
        val stageStr = data["stage"] as? String ?: "UNKNOWN"
        val stage = try {
            AnemiaStage.valueOf(stageStr.uppercase())
        } catch (_: Exception) {
            AnemiaStage.UNKNOWN
        }

        return AnemiaResult(
            id = data["id"] as? String ?: "",
            patientId = data["patientId"] as? String ?: "",
            patientName = data["patientName"] as? String ?: "",
            patientAge = (data["patientAge"] as? Number)?.toInt() ?: 0,
            patientGender = data["patientGender"] as? String ?: "",
            patientDetailsJson = data["patientDetailsJson"] as? String ?: "",
            hemoglobinEstimate = (data["hemoglobinEstimate"] as? Number)?.toFloat() ?: 0f,
            stage = stage,
            confidence = (data["confidence"] as? Number)?.toFloat() ?: 0f,
            explanation = data["explanation"] as? String ?: "",
            perImageFindings = data["perImageFindings"] as? Map<String, String> ?: emptyMap(),
            ayurvedicInsights = data["ayurvedicInsights"] as? Map<String, String> ?: emptyMap(),
            providerUsed = data["providerUsed"] as? String ?: "unknown",
            imageUrls = data["imageUrls"] as? List<String> ?: emptyList(),
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
        )
    }

    /**
     * Save anonymized copy to training_data collection.
     * This data is used for model retraining — contains images + results
     * but NO personal identifiers (stored under random scan ID).
     */
    private suspend fun saveTrainingData(result: AnemiaResult) {
        try {
            val trainingDoc = mapOf(
                "scanId" to result.id,
                "imageUrls" to result.imageUrls,
                "hemoglobinEstimate" to result.hemoglobinEstimate,
                "stage" to result.stage.name,
                "confidence" to result.confidence,
                "explanation" to result.explanation,
                "perImageFindings" to result.perImageFindings,
                "providerUsed" to result.providerUsed,
                "timestamp" to result.timestamp,
                // Anonymized: no patientId, name, or email
                "consentGiven" to true,
                "dataVersion" to 1
            )

            firestore.collection("training_data")
                .document(result.id)
                .set(trainingDoc)
                .await()
        } catch (_: Exception) {
            // Training data save is best-effort, don't fail the main flow
        }
    }

    /**
     * Update the user's scan statistics.
     */
    private suspend fun updateUserScanStats(userId: String) {
        try {
            val userRef = firestore.collection("users").document(userId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCount = snapshot.getLong("totalScans") ?: 0
                transaction.update(userRef, mapOf(
                    "totalScans" to currentCount + 1,
                    "lastScanAt" to System.currentTimeMillis()
                ))
            }.await()
        } catch (_: Exception) {}
    }

    /**
     * Get training data stats (admin use).
     */
    suspend fun getTrainingDataCount(): Int {
        return try {
            firestore.collection("training_data")
                .get().await()
                .size()
        } catch (e: Exception) {
            0
        }
    }
}
