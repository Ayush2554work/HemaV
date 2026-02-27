package com.meditech.hemav.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.meditech.hemav.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * HTTP client for the HemaV FastAPI backend.
 *
 * Base URL is injected from BuildConfig (loaded from secrets.properties).
 * JWT token is persisted in SharedPreferences after login/register.
 *
 * All calls are synchronous — wrap in withContext(Dispatchers.IO).
 */
object HemavApiClient {

    private val BASE_URL get() = BuildConfig.BACKEND_BASE_URL.trimEnd('/')
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // ── Token Management ─────────────────────────────
    private var _token: String? = null

    fun setToken(token: String) { _token = token }
    fun clearToken() { _token = null }
    fun hasToken(): Boolean = !_token.isNullOrBlank()

    fun saveToken(context: Context, token: String) {
        prefs(context).edit().putString("hemav_jwt", token).apply()
        _token = token
    }

    fun loadToken(context: Context) {
        _token = prefs(context).getString("hemav_jwt", null)
    }

    fun clearToken(context: Context) {
        prefs(context).edit().remove("hemav_jwt").apply()
        _token = null
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("hemav_prefs", Context.MODE_PRIVATE)

    // ── Auth ─────────────────────────────────────────

    /**
     * Register a new user on the backend.
     * Returns the JWT token response as JSONObject.
     */
    fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: String,
    ): JSONObject {
        val body = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", password)
            put("phone", phone)
            put("role", role)
        }
        return post("/auth/register", body)
    }

    /**
     * Login and get JWT token.
     */
    fun login(email: String, password: String): JSONObject {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        return post("/auth/login", body)
    }

    // ── Health ────────────────────────────────────────

    fun healthCheck(): JSONObject = get("/health")

    // ── Scans ─────────────────────────────────────────

    /**
     * Save anemia scan result to the backend.
     */
    fun saveScanResult(
        riskLevel: String,
        confidence: Float,
        hemoglobinEstimate: String,
        details: String,
        recommendations: List<String>,
    ): JSONObject {
        val body = JSONObject().apply {
            put("risk_level", riskLevel)
            put("confidence", confidence)
            put("hemoglobin_estimate", hemoglobinEstimate)
            put("details", details)
            put("recommendations", JSONArray(recommendations))
            put("image_urls", JSONArray())
        }
        return postAuth("/scans/", body)
    }

    fun listScans(): JSONArray = getAuthArray("/scans/")

    // ── Appointments ──────────────────────────────────

    fun createAppointment(
        doctorId: String,
        date: String,
        time: String,
        type: String,
        notes: String,
    ): JSONObject {
        val body = JSONObject().apply {
            put("doctor_id", doctorId)
            put("date", date)
            put("time", time)
            put("type", type)
            put("notes", notes)
        }
        return postAuth("/appointments/", body)
    }

    fun listAppointments(): JSONArray = getAuthArray("/appointments/")

    // ── HTTP Helpers ──────────────────────────────────

    private fun get(path: String): JSONObject {
        val req = Request.Builder().url("$BASE_URL$path").get().build()
        val resp = http.newCall(req).execute()
        val body = resp.body?.string() ?: "{}"
        if (!resp.isSuccessful) throw RuntimeException("API error ${resp.code}: $body")
        return JSONObject(body)
    }

    private fun getAuthArray(path: String): JSONArray {
        val token = _token ?: throw RuntimeException("Not authenticated")
        val req = Request.Builder()
            .url("$BASE_URL$path")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val resp = http.newCall(req).execute()
        val body = resp.body?.string() ?: "[]"
        if (!resp.isSuccessful) throw RuntimeException("API error ${resp.code}: $body")
        return JSONArray(body)
    }

    private fun post(path: String, json: JSONObject): JSONObject {
        val req = Request.Builder()
            .url("$BASE_URL$path")
            .post(json.toString().toRequestBody(JSON))
            .build()
        val resp = http.newCall(req).execute()
        val body = resp.body?.string() ?: "{}"
        if (!resp.isSuccessful) throw RuntimeException("API error ${resp.code}: $body")
        return JSONObject(body)
    }

    private fun postAuth(path: String, json: JSONObject): JSONObject {
        val token = _token ?: throw RuntimeException("Not authenticated")
        val req = Request.Builder()
            .url("$BASE_URL$path")
            .addHeader("Authorization", "Bearer $token")
            .post(json.toString().toRequestBody(JSON))
            .build()
        val resp = http.newCall(req).execute()
        val body = resp.body?.string() ?: "{}"
        if (!resp.isSuccessful) throw RuntimeException("API error ${resp.code}: $body")
        return JSONObject(body)
    }
}
