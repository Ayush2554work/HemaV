package com.meditech.hemav.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.meditech.hemav.data.model.Prescription
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

class PrescriptionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val prescriptionsCollection = firestore.collection("prescriptions")

    suspend fun uploadPrescriptionPdf(
        doctorId: String,
        patientId: String,
        prescriptionId: String,
        pdfFile: File
    ): Result<String> {
        return try {
            val ref = storage.reference
                .child("prescriptions")
                .child(doctorId)
                .child(patientId)
                .child("$prescriptionId.pdf")

            val uri = android.net.Uri.fromFile(pdfFile)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("PrescriptionRepo", "Error uploading PDF", e)
            Result.failure(e)
        }
    }

    suspend fun createPrescription(prescription: Prescription): Result<String> {
        return try {
            val docRef = prescriptionsCollection.document()
            val newPrescription = prescription.copy(id = docRef.id)
            docRef.set(newPrescription).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("PrescriptionRepo", "Error creating prescription", e)
            Result.failure(e)
        }
    }

    suspend fun getPrescriptionsForPatient(patientId: String): Result<List<Prescription>> {
        return try {
            val snapshot = prescriptionsCollection
                .whereEqualTo("patientId", patientId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val prescriptions = snapshot.toObjects(Prescription::class.java)
            Result.success(prescriptions)
        } catch (e: Exception) {
            Log.e("PrescriptionRepo", "Error getting prescriptions", e)
            Result.failure(e)
        }
    }

    suspend fun getPrescriptionsForDoctor(doctorId: String): Result<List<Prescription>> {
        return try {
            val snapshot = prescriptionsCollection
                .whereEqualTo("doctorId", doctorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val prescriptions = snapshot.toObjects(Prescription::class.java)
            Result.success(prescriptions)
        } catch (e: Exception) {
            Log.e("PrescriptionRepo", "Error getting prescriptions", e)
            Result.failure(e)
        }
    }
}
