package com.meditech.hemav.data.model

/**
 * User roles in HemaV
 */
enum class UserRole {
    PATIENT,
    DOCTOR
}

/**
 * Base user data stored in Firestore
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.PATIENT,
    val profilePicUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Patient-specific profile data
 */
data class PatientProfile(
    val uid: String = "",
    val dateOfBirth: String = "",
    val bloodGroup: String = "",
    val gender: String = "",
    val address: String = "",
    val medicalHistory: List<String> = emptyList(),
    val allergies: List<String> = emptyList()
)

/**
 * Doctor-specific profile data
 */
data class DoctorProfile(
    val uid: String = "",
    val name: String = "",
    val licenseNumber: String = "",
    val specialties: List<String> = emptyList(),
    val qualifications: String = "",
    val degree: String = "",
    val experience: Int = 0, // years
    val about: String = "",
    val clinicAddress: String = "",
    val city: String = "",
    val consultationFee: Double = 0.0,
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val isVerified: Boolean = false,
    val profilePicUrl: String = "",
    val availableSlots: List<String> = emptyList(), // "Mon 09:00-17:00"
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/**
 * Appointment between patient and doctor
 */
data class Appointment(
    val id: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val date: String = "",
    val time: String = "",
    val timestamp: Long = 0,
    val type: AppointmentType = AppointmentType.VIDEO,
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val notes: String = "",
    val patientAge: String = "",
    val patientGender: String = "",
    val patientBloodGroup: String = "",
    val patientWeight: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class AppointmentType { VIDEO, IN_PERSON }
enum class AppointmentStatus { PENDING, CONFIRMED, COMPLETED, CANCELLED }

/**
 * Prescription issued by doctor
 */
data class Prescription(
    val id: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val appointmentId: String = "",
    val medicines: List<Medicine> = emptyList(),
    val diagnosis: String = "",
    val notes: String = "",
    val pdfUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Medicine(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "", // "Twice daily", "Once at night"
    val duration: String = "", // "7 days", "2 weeks"
    val instructions: String = "" // "After food", "Before sleep"
)

/**
 * Chat message between patient and doctor
 */
data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "", // "image", "pdf", "report"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class ChatRoom(
    val id: String = "", // e.g. "patientId_doctorId"
    val patientId: String = "",
    val doctorId: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0L,
    val patientName: String = "",
    val doctorName: String = ""
)


