package com.meditech.hemav.data.model

/**
 * Patient demographic and medical details collected before anemia screening.
 * These are sent to the AI model for more accurate analysis.
 */
data class PatientDetails(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",          // Male, Female, Other
    val ethnicity: String = "",       // South Asian, African, Caucasian, East Asian, Hispanic, Middle Eastern, Other
    val region: String = "",          // Geographic region for dietary/environmental context
    val weight: Float = 0f,           // kg
    val dietType: String = "",        // Vegetarian, Non-Vegetarian, Vegan, Eggetarian
    val knownConditions: String = "", // E.g., Diabetes, Thyroid, Sickle Cell, Thalassemia
    val currentSymptoms: String = "", // Fatigue, dizziness, pale skin, shortness of breath, etc.
    val menstrualHistory: String = "", // For females: regular/irregular/heavy/pregnant/postpartum
    val previousAnemia: Boolean = false,
    val currentMedications: String = ""
)
