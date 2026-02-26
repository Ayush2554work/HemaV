package com.meditech.hemav.ai.providers

import android.graphics.Bitmap
import android.util.Base64
import com.meditech.hemav.BuildConfig
import com.meditech.hemav.ai.LlmProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Groq provider using Llama 3.2 Vision (fallback 1).
 * Free tier: ~7,000 requests/day via Groq's ultra-fast inference.
 */
class GroqProvider : LlmProvider {
    override val name = "Groq (Llama 4 Scout Vision)"
    override val isAvailable: Boolean
        get() = BuildConfig.GROQ_API_KEY.isNotBlank()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    override suspend fun analyzeImages(images: List<Bitmap>, prompt: String): String {
        return withContext(Dispatchers.IO) {
            // Build multimodal message with base64 images
            val contentArray = JsonArray()

            // Add text part
            val textPart = JsonObject().apply {
                addProperty("type", "text")
                addProperty("text", prompt)
            }
            contentArray.add(textPart)

            // Add image parts (Llama 4 Scout supports up to 5 images)
            images.forEach { bitmap ->
                val base64 = bitmapToBase64(bitmap)
                val imagePart = JsonObject().apply {
                    addProperty("type", "image_url")
                    add("image_url", JsonObject().apply {
                        addProperty("url", "data:image/jpeg;base64,$base64")
                    })
                }
                contentArray.add(imagePart)
            }

            val message = JsonObject().apply {
                addProperty("role", "user")
                add("content", contentArray)
            }

            val messagesArray = JsonArray().apply { add(message) }

            val body = JsonObject().apply {
                addProperty("model", "meta-llama/llama-4-scout-17b-16e-instruct")
                add("messages", messagesArray)
                addProperty("temperature", 0.3)
                addProperty("max_tokens", 4096)
            }

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("Groq API error: ${response.code} - ${response.body?.string()}")
            }

            val responseBody = response.body?.string() ?: throw Exception("Empty Groq response")
            val jsonResponse = com.google.gson.Gson().fromJson(responseBody, JsonObject::class.java)
            jsonResponse
                .getAsJsonArray("choices")
                .get(0).asJsonObject
                .getAsJsonObject("message")
                .get("content").asString
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        // Resize for API limits
        val scaled = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
