package com.meditech.hemav.data.model

/**
 * WHO Anemia Stages based on hemoglobin levels
 */
enum class AnemiaStage(val label: String, val description: String) {
    NORMAL("Normal", "Hemoglobin ≥ 12 g/dL — No anemia detected"),
    MILD("Mild Anemia", "Hemoglobin 11–11.9 g/dL — Mild iron deficiency"),
    MODERATE("Moderate Anemia", "Hemoglobin 8–10.9 g/dL — Seek medical attention"),
    SEVERE("Severe Anemia", "Hemoglobin < 8 g/dL — Urgent medical care needed"),
    UNKNOWN("Unknown", "Unable to determine — please consult a doctor"),
    INVALID("Invalid Images", "Photos do not appear to be of human face/eye/hands")
}

data class AnemiaResult(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientAge: Int = 0,
    val patientGender: String = "",
    val patientDetailsJson: String = "", // Full PatientDetails serialized as JSON
    val hemoglobinEstimate: Float = 0f,
    val stage: AnemiaStage = AnemiaStage.UNKNOWN,
    val confidence: Float = 0f,
    val explanation: String = "",
    val perImageFindings: Map<String, String> = emptyMap(),
    val ayurvedicInsights: Map<String, String> = emptyMap(),
    val providerUsed: String = "unknown",
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
