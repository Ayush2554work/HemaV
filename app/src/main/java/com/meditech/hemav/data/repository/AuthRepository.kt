package com.meditech.hemav.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.meditech.hemav.data.model.User
import com.meditech.hemav.data.model.UserRole
import com.meditech.hemav.data.remote.HemavApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for authentication.
 *
 * Uses Firebase Auth as the primary identity provider.
 * After successful login/register, syncs with the HemaV backend
 * to get a JWT token for accessing backend-only APIs (scans, appointments, etc.)
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    suspend fun login(
        email: String,
        password: String,
        context: Context? = null,
    ): Result<FirebaseUser> {
        return try {
            // 1. Firebase login
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!

            // 2. Sync with backend (best-effort — don't fail login if backend is down)
            try {
                withContext(Dispatchers.IO) {
                    val resp = HemavApiClient.login(email, password)
                    val token = resp.getString("access_token")
                    if (context != null) HemavApiClient.saveToken(context, token)
                    else HemavApiClient.setToken(token)
                }
            } catch (e: Exception) {
                android.util.Log.w("AuthRepository", "Backend sync failed (non-critical): ${e.message}")
            }

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole,
        context: Context? = null,
    ): Result<FirebaseUser> {
        return try {
            // 1. Firebase register
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!

            // 2. Store user in Firestore
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                phone = phone,
                role = role
            )
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            // 3. Register on backend (best-effort)
            try {
                withContext(Dispatchers.IO) {
                    val resp = HemavApiClient.register(
                        name = name,
                        email = email,
                        password = password,
                        phone = phone,
                        role = role.name,
                    )
                    val token = resp.getString("access_token")
                    if (context != null) HemavApiClient.saveToken(context, token)
                    else HemavApiClient.setToken(token)
                }
            } catch (e: Exception) {
                android.util.Log.w("AuthRepository", "Backend register failed (non-critical): ${e.message}")
            }

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(uid: String): UserRole? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getString("role")?.let { UserRole.valueOf(it) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun logout(context: Context? = null) {
        auth.signOut()
        if (context != null) HemavApiClient.clearToken(context)
        else HemavApiClient.clearToken()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDoctorProfile(uid: String, profile: com.meditech.hemav.data.model.DoctorProfile) {
        try {
            firestore.collection("doctors").document(uid).set(profile).await()
        } catch (_: Exception) {}
    }

    /**
     * Call on app startup to restore JWT token from storage.
     */
    fun restoreSession(context: Context) {
        HemavApiClient.loadToken(context)
    }
}
