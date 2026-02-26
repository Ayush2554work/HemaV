package com.meditech.hemav.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.meditech.hemav.data.model.DoctorProfile
import com.meditech.hemav.data.model.Appointment
import com.meditech.hemav.data.model.AppointmentStatus
import kotlinx.coroutines.tasks.await

class DoctorRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val doctorsCollection = firestore.collection("doctors")
    private val appointmentsCollection = firestore.collection("appointments")

    suspend fun searchDoctors(
        specialty: String? = null,
        minRating: Float = 0f,
        maxFee: Double = Double.MAX_VALUE
    ): List<DoctorProfile> {
        return try {
            var query: Query = doctorsCollection

            if (specialty != null && specialty.isNotBlank()) {
                query = query.whereArrayContains("specialties", specialty)
            }

            val snapshot = query.get().await()
            snapshot.toObjects(DoctorProfile::class.java)
                .filter { it.rating >= minRating && it.consultationFee <= maxFee }
                .sortedByDescending { it.rating }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDoctorProfile(doctorId: String): DoctorProfile? {
        return try {
            doctorsCollection.document(doctorId).get().await()
                .toObject(DoctorProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun bookAppointment(appointment: Appointment): Result<String> {
        return try {
            val docRef = appointmentsCollection.document()
            val apptWithId = appointment.copy(id = docRef.id)
            docRef.set(apptWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointments(userId: String, isDoctor: Boolean): List<Appointment> {
        return try {
            val field = if (isDoctor) "doctorId" else "patientId"
            appointmentsCollection
                .whereEqualTo(field, userId)
                .get().await()
                .toObjects(Appointment::class.java)
                .sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDemoDoctors(): List<DoctorProfile> = listOf(
        DoctorProfile(
            uid = "demo_doc_1",
            name = "Dr. Mehta",
            licenseNumber = "AYU-2024-1001",
            specialties = listOf("Panchakarma", "General Ayurveda"),
            qualifications = "BAMS, MD (Ayurveda)",
            experience = 12,
            about = "Specializing in Panchakarma treatments and chronic disease management through Ayurvedic principles.",
            clinicAddress = "Ayush Wellness Center, Sector 15, Noida",
            consultationFee = 300.0,
            rating = 4.8f,
            totalRatings = 245,
            isVerified = true,
            availableSlots = listOf("Mon 09:00-13:00", "Wed 09:00-13:00", "Fri 14:00-18:00")
        ),
        DoctorProfile(
            uid = "demo_doc_2",
            name = "Dr. Sharma",
            licenseNumber = "AYU-2024-1002",
            specialties = listOf("Rasayana", "Women's Health", "Nutrition"),
            qualifications = "BAMS, MS (Prasuti Tantra)",
            experience = 8,
            about = "Expert in women's health, Rasayana therapy, and nutritional counseling with Ayurvedic diet planning.",
            clinicAddress = "Vaidya Clinic, Koramangala, Bangalore",
            consultationFee = 250.0,
            rating = 4.6f,
            totalRatings = 178,
            isVerified = true,
            availableSlots = listOf("Tue 10:00-14:00", "Thu 10:00-14:00", "Sat 09:00-12:00")
        ),
        DoctorProfile(
            uid = "demo_doc_3",
            name = "Dr. Rao",
            licenseNumber = "AYU-2024-1003",
            specialties = listOf("Raktamokshana", "Hematology", "General Ayurveda"),
            qualifications = "BAMS, PhD (Kayachikitsa)",
            experience = 15,
            about = "Leading Ayurvedic hematologist specializing in anemia treatment through traditional Raktamokshana and herbal formulations.",
            clinicAddress = "Charaka Ayurveda Hospital, Banjara Hills, Hyderabad",
            consultationFee = 500.0,
            rating = 4.9f,
            totalRatings = 312,
            isVerified = true,
            availableSlots = listOf("Mon 10:00-16:00", "Wed 10:00-16:00", "Fri 10:00-16:00")
        ),
        DoctorProfile(
            uid = "demo_doc_4",
            name = "Dr. Gupta",
            licenseNumber = "AYU-2024-1004",
            specialties = listOf("Nadi Pariksha", "Yoga Therapy"),
            qualifications = "BAMS, Cert. Yoga Therapy",
            experience = 6,
            about = "Integrative approach combining Nadi Pariksha diagnostics with therapeutic yoga for holistic healing.",
            clinicAddress = "Patanjali Wellness, Dwarka, New Delhi",
            consultationFee = 200.0,
            rating = 4.4f,
            totalRatings = 89,
            isVerified = true,
            availableSlots = listOf("Mon-Sat 08:00-12:00")
        )
    )
}
