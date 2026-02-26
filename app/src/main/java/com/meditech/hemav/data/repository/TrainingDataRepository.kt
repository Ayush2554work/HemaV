package com.meditech.hemav.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing training data collected from user scans.
 *
 * Training data is stored anonymized in Firestore:
 *   /training_data/{scanId} → {imageUrls, hemoglobinEstimate, stage, confidence, ...}
 *
 * This data can be exported to train or fine-tune ML models for anemia detection.
 * No personal identifiers (name, email, userId) are stored in training documents.
 *
 * Firebase Storage structure for images:
 *   /scans/{userId}/{scanId}/photo_0.jpg ... photo_4.jpg
 *
 * Note: Images in Storage are referenced by URL in training_data docs.
 * For actual model training, a backend Cloud Function or pipeline should:
 *   1. Query /training_data collection
 *   2. Download images from Storage URLs
 *   3. Pair with labels (hemoglobin, stage)
 *   4. Feed into training pipeline
 */
class TrainingDataRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val trainingCollection = firestore.collection("training_data")

    /**
     * Get all training data entries.
     * Each entry contains:
     *   - scanId: unique identifier
     *   - imageUrls: list of 5 Firebase Storage download URLs
     *   - hemoglobinEstimate: float (g/dL)
     *   - stage: NORMAL / MILD / MODERATE / SEVERE
     *   - confidence: float (0-1)
     *   - explanation: AI model's reasoning
     *   - perImageFindings: map of region → finding
     *   - providerUsed: which LLM generated the analysis
     *   - timestamp: when the scan was done
     *   - consentGiven: boolean
     *   - dataVersion: int
     */
    suspend fun getAllTrainingData(): List<Map<String, Any>> {
        return try {
            trainingCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
                .documents.mapNotNull { it.data }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get training data filtered by anemia stage (for balanced training sets).
     */
    suspend fun getTrainingDataByStage(stage: String): List<Map<String, Any>> {
        return try {
            trainingCollection
                .whereEqualTo("stage", stage)
                .get().await()
                .documents.mapNotNull { it.data }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get total count of training samples.
     */
    suspend fun getTrainingDataCount(): Int {
        return try {
            trainingCollection.get().await().size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get distribution of anemia stages in training data.
     * Returns map like: {NORMAL: 42, MILD: 28, MODERATE: 15, SEVERE: 5}
     */
    suspend fun getStageDistribution(): Map<String, Int> {
        return try {
            val allDocs = trainingCollection.get().await()
            allDocs.documents
                .groupBy { it.getString("stage") ?: "UNKNOWN" }
                .mapValues { it.value.size }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Get training entries from a specific date range.
     */
    suspend fun getTrainingDataInRange(startMs: Long, endMs: Long): List<Map<String, Any>> {
        return try {
            trainingCollection
                .whereGreaterThanOrEqualTo("timestamp", startMs)
                .whereLessThanOrEqualTo("timestamp", endMs)
                .get().await()
                .documents.mapNotNull { it.data }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Delete a training data entry (e.g., for data quality issues).
     */
    suspend fun deleteTrainingEntry(scanId: String): Result<Unit> {
        return try {
            trainingCollection.document(scanId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export metadata summary for training pipeline.
     * Returns a list of (imageUrlList, label) pairs ready for ML ingestion.
     */
    suspend fun exportForTraining(): List<TrainingEntry> {
        return try {
            trainingCollection
                .whereEqualTo("consentGiven", true)
                .get().await()
                .documents.mapNotNull { doc ->
                    val urls = doc.get("imageUrls") as? List<String> ?: return@mapNotNull null
                    val hb = doc.getDouble("hemoglobinEstimate")?.toFloat() ?: return@mapNotNull null
                    val stage = doc.getString("stage") ?: return@mapNotNull null
                    val confidence = doc.getDouble("confidence")?.toFloat() ?: 0f
                    TrainingEntry(
                        scanId = doc.id,
                        imageUrls = urls,
                        hemoglobinEstimate = hb,
                        stage = stage,
                        confidence = confidence
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Structured training entry for ML pipeline export.
 */
data class TrainingEntry(
    val scanId: String,
    val imageUrls: List<String>,
    val hemoglobinEstimate: Float,
    val stage: String,       // NORMAL, MILD, MODERATE, SEVERE
    val confidence: Float
)
