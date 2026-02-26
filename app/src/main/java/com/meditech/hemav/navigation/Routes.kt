package com.meditech.hemav.navigation

/**
 * Navigation route constants for HemaV app.
 */
object Routes {
    // Auth
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Patient
    const val PATIENT_DASHBOARD = "patient_dashboard"
    const val ANEMIA_SCAN = "anemia_scan"
    const val SCAN_HISTORY = "scan_history"
    const val SCAN_DETAIL = "scan_detail/{scanId}"
    
    fun scanDetail(scanId: String) = "scan_detail/$scanId"
    const val ANEMIA_CAPTURE = "anemia_capture/{step}"
    const val ANEMIA_RESULTS = "anemia_results"
    const val DOCTOR_SEARCH = "doctor_search"
    const val DOCTOR_DETAIL = "doctor_detail/{doctorId}"
    const val BOOK_APPOINTMENT = "book_appointment/{doctorId}"
    const val PATIENT_CHAT_LIST = "patient_chat_list"
    const val CHAT = "chat/{chatId}"
    const val MY_REPORTS = "my_reports"
    const val MEDICATIONS = "medications"
    const val HEALTH_INSIGHTS = "health_insights"
    const val MEDICAL_STORE = "medical_store"
    const val CART = "cart"
    const val COMING_SOON = "coming_soon/{featureName}"

    fun comingSoon(featureName: String) = "coming_soon/$featureName"

    // Doctor
    const val DOCTOR_DASHBOARD = "doctor_dashboard"
    const val DOCTOR_PROFILE_SETUP = "doctor_profile_setup"
    const val APPOINTMENT_REQUESTS = "appointment_requests"
    const val DOCTOR_CHAT_LIST = "doctor_chat_list"
    const val CREATE_PRESCRIPTION = "create_prescription/{patientId}?patientName={patientName}"
    const val VIDEO_CALL = "video_call/{appointmentId}"

    // Shared
    const val PATIENT_APPOINTMENTS = "patient_appointments"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val LEGAL = "legal"

    // Helper functions for parameterized routes
    fun anemiaCapture(step: Int) = "anemia_capture/$step"
    fun doctorDetail(doctorId: String) = "doctor_detail/$doctorId"
    fun bookAppointment(doctorId: String) = "book_appointment/$doctorId"
    fun chat(chatId: String) = "chat/$chatId"
    fun createPrescription(patientId: String, patientName: String) = "create_prescription/$patientId?patientName=$patientName"
    fun videoCall(appointmentId: String) = "video_call/$appointmentId"

    // Forum
    const val FORUM_LIST = "forum_list"
    const val FORUM_DETAIL = "forum_detail/{postId}"
    
    fun forumDetail(postId: String) = "forum_detail/$postId"
}
