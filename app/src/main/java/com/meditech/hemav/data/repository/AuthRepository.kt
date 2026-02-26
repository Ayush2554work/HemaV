package com.meditech.hemav.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.meditech.hemav.data.model.User
import com.meditech.hemav.data.model.UserRole
import kotlinx.coroutines.tasks.await

/**
 * Repository for Firebase Authentication operations
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = currentUser != null

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!

            // Create user document in Firestore
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

    fun logout() {
        auth.signOut()
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
}
