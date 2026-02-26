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
 * HuggingFace Inference API provider (fallback 2).
 * Uses the Serverless Inference Router (OpenAI-compatible).
 * Model: meta-llama/Llama-3.2-11B-Vision-Instruct (or Qwen2-VL)
 */
class HuggingFaceProvider : LlmProvider {
    override val name = "HuggingFace (Vision)"
    override val isAvailable: Boolean
        get() = BuildConfig.HUGGINGFACE_API_KEY.isNotBlank()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    // Using OpenAI-compatible router endpoint
    private val routerUrl = "https://router.huggingface.co/v1/chat/completions"
    
    // Using Qwen2.5-VL (State of the art open vision model)
    private val modelId = "Qwen/Qwen2.5-VL-72B-Instruct" 

    override suspend fun analyzeImages(images: List<Bitmap>, prompt: String): String {
        return withContext(Dispatchers.IO) {
            val contentArray = JsonArray()

            // Text part
            contentArray.add(JsonObject().apply {
                addProperty("type", "text")
                addProperty("text", prompt)
            })

            // Send ALL images for multi-image analysis
            if (images.isEmpty()) return@withContext "No images provided"

            images.forEach { bitmap ->
                val base64 = bitmapToBase64(bitmap)
                contentArray.add(JsonObject().apply {
                    addProperty("type", "image_url")
                    add("image_url", JsonObject().apply {
                        addProperty("url", "data:image/jpeg;base64,$base64")
                    })
                })
            }

            val message = JsonObject().apply {
                addProperty("role", "user")
                add("content", contentArray)
            }

            val body = JsonObject().apply {
                addProperty("model", modelId)
                add("messages", JsonArray().apply { add(message) })
                addProperty("max_tokens", 1024)
                addProperty("temperature", 0.3)
            }

            val request = Request.Builder()
                .url(routerUrl)
                .addHeader("Authorization", "Bearer ${BuildConfig.HUGGINGFACE_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                 // Try parsing error body
                 val errBody = response.body?.string()
                 throw Exception("HuggingFace API error: ${response.code} - $errBody")
            }

            val responseBody = response.body?.string() ?: throw Exception("Empty HF response")
            
            // OpenAI format response
            try {
                val jsonResponse = com.google.gson.Gson().fromJson(responseBody, JsonObject::class.java)
                jsonResponse.getAsJsonArray("choices")
                    .get(0).asJsonObject
                    .getAsJsonObject("message")
                    .get("content").asString
            } catch (e: Exception) {
                // Fallback if format differs
                responseBody
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        val scaled = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
