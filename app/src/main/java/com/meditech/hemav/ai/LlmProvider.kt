package com.meditech.hemav.ai

import android.graphics.Bitmap

/**
 * Common interface for all LLM providers used for anemia detection.
 * Each provider takes images and a prompt, returns structured results.
 */
interface LlmProvider {
    val name: String
    val isAvailable: Boolean

    /**
     * Analyze images for anemia indicators.
     * @param images List of bitmaps (face, tongue, eye, palm, nails)
     * @param prompt Medical analysis prompt
     * @return Raw JSON string response from the LLM
     */
    suspend fun analyzeImages(images: List<Bitmap>, prompt: String): String
}
