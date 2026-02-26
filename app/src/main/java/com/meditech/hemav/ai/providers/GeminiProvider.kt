package com.meditech.hemav.ai.providers

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.meditech.hemav.BuildConfig
import com.meditech.hemav.ai.LlmProvider

/**
 * Google Gemini 2.0 Flash provider (primary).
 * Free tier: ~1,500 requests/day, multimodal support.
 */
class GeminiProvider : LlmProvider {
    override val name = "Gemini 2.0 Flash"
    override val isAvailable: Boolean
        get() = BuildConfig.GEMINI_API_KEY.isNotBlank()

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    override suspend fun analyzeImages(images: List<Bitmap>, prompt: String): String {
        val content = content {
            images.forEach { bitmap ->
                image(bitmap)
            }
            text(prompt)
        }
        val response = model.generateContent(content)
        return response.text ?: throw Exception("Empty response from Gemini")
    }
}
