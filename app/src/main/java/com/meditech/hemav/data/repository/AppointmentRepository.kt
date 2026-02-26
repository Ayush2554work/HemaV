package com.meditech.hemav.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.meditech.hemav.data.model.Appointment
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AppointmentRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val appointmentsCollection = firestore.collection("appointments")

    suspend fun createAppointment(appointment: Appointment): Result<String> {
        return try {
            val docRef = appointmentsCollection.document()
            val newAppointment = appointment.copy(id = docRef.id)
            docRef.set(newAppointment).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

     fun getAppointmentsForPatient(patientId: String): Flow<List<Appointment>> = callbackFlow {
        val listener = appointmentsCollection
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val appointments = snapshot?.toObjects(Appointment::class.java)
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }

    fun getAppointmentsForDoctor(doctorId: String): Flow<List<Appointment>> = callbackFlow {
        val listener = appointmentsCollection
            .whereEqualTo("doctorId", doctorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val appointments = snapshot?.toObjects(Appointment::class.java)
                    ?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(appointments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: com.meditech.hemav.data.model.AppointmentStatus): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
