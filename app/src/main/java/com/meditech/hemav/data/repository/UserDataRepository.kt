package com.meditech.hemav.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.meditech.hemav.data.model.User
import com.meditech.hemav.data.model.PatientProfile
import com.meditech.hemav.data.model.DoctorProfile
import com.meditech.hemav.data.model.UserRole
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

/**
 * Repository for user data management in Firebase.
 *
 * Firestore structure:
 *   /users/{uid}           → User base document (name, email, role, phone, timestamps)
 *   /users/{uid}/scans/    → Anemia scan results (managed by ScanRepository)
 *   /patients/{uid}        → PatientProfile (DOB, blood group, medical history)
 *   /doctors/{uid}         → DoctorProfile (license, specialties, fees, ratings)
 *
 * Storage structure:
 *   /profile_pictures/{uid}.jpg → Profile photo
 *   /scans/{uid}/{scanId}/     → Scan photos (managed by ScanRepository)
 */
class UserDataRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ==================== USER PROFILE ====================

    /**
     * Get current authenticated user's profile from Firestore.
     */
    suspend fun getCurrentUserProfile(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return getUserProfile(uid)
    }

    /**
     * Get any user's profile by UID.
     */
    suspend fun getUserProfile(uid: String): User? {
        return try {
            firestore.collection("users")
                .document(uid)
                .get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update user profile fields (name, phone, etc.)
     */
    suspend fun updateUserProfile(
        uid: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload profile picture to Firebase Storage and update Firestore URL.
     */
    suspend fun uploadProfilePicture(uid: String, bitmap: Bitmap): Result<String> {
        return try {
            val ref = storage.reference
                .child("profile_pictures")
                .child("$uid.jpg")

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            ref.putBytes(baos.toByteArray()).await()

            val downloadUrl = ref.downloadUrl.await().toString()

            // Update user document with photo URL
            firestore.collection("users")
                .document(uid)
                .update("profilePicUrl", downloadUrl)
                .await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== PATIENT PROFILE ====================

    /**
     * Save or update patient-specific profile data.
     */
    suspend fun savePatientProfile(profile: PatientProfile): Result<Unit> {
        return try {
            firestore.collection("patients")
                .document(profile.uid)
                .set(profile)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get patient profile.
     */
    suspend fun getPatientProfile(uid: String): PatientProfile? {
        return try {
            firestore.collection("patients")
                .document(uid)
                .get().await()
                .toObject(PatientProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== DOCTOR PROFILE ====================

    /**
     * Save or update doctor-specific profile data.
     */
    suspend fun saveDoctorProfile(profile: DoctorProfile): Result<Unit> {
        return try {
            firestore.collection("doctors")
                .document(profile.uid)
                .set(profile)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get doctor profile.
     */
    suspend fun getDoctorProfile(uid: String): DoctorProfile? {
        return try {
            firestore.collection("doctors")
                .document(uid)
                .get().await()
                .toObject(DoctorProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ==================== ACCOUNT MANAGEMENT ====================

    /**
     * Delete all user data (GDPR compliance).
     */
    suspend fun deleteUserData(uid: String): Result<Unit> {
        return try {
            // Delete user profile
            firestore.collection("users").document(uid).delete().await()
            // Delete patient/doctor profile
            firestore.collection("patients").document(uid).delete().await()
            firestore.collection("doctors").document(uid).delete().await()
            // Delete profile picture
            try {
                storage.reference.child("profile_pictures/$uid.jpg").delete().await()
            } catch (_: Exception) {}
            // Note: Scan data in training_data is anonymized, so it stays
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all active users count (admin use).
     */
    suspend fun getTotalUsersCount(): Int {
        return try {
            firestore.collection("users").get().await().size()
        } catch (e: Exception) {
            0
        }
    }
}
