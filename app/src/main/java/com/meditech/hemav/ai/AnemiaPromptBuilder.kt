package com.meditech.hemav.ai

import com.meditech.hemav.data.model.PatientDetails

/**
 * Builds the medical analysis prompt for anemia detection from images.
 * Optionally includes patient demographic and medical details for more accurate analysis.
 */
object AnemiaPromptBuilder {

    fun buildPrompt(patientDetails: PatientDetails? = null): String {
        val patientContext = if (patientDetails != null) buildPatientContext(patientDetails) else ""

        return """
You are a medical AI assistant specialized in non-invasive anemia screening with expertise in Ayurvedic medicine. 
You are analyzing 5 photos of a patient: face, tongue, lower eyelid (conjunctiva), palm/wrist, and fingernail beds.
$patientContext
**Your task:** Analyze visual indicators of anemia in these images and provide a screening assessment with both modern medical analysis and Ayurvedic (traditional Indian medicine) insights.
First, verify that the images are of a human patient (Face, Tongue, Eye, or Hand). If they are not (e.g., furniture, scenery, animals), yield an INVALID result.

**Clinical indicators to evaluate:**
1. **Face**: Overall skin pallor, facial color compared to normal healthy complexion
2. **Tongue**: Color intensity - healthy is bright red/pink, anemic is pale/whitish
3. **Conjunctiva (lower eyelid)**: Redness level - healthy is rich red, anemic is pale pink/white
4. **Palm**: Color of palm creases - healthy are visibly red/pink, anemic creases are pale
5. **Fingernail beds**: Press-and-release color return (capillary refill), overall nail bed pinkness

**WHO Anemia Classification (Hemoglobin levels):**
- Normal: ≥ 12.0 g/dL (women), ≥ 13.0 g/dL (men)
- Mild anemia: 11.0 – 11.9 g/dL
- Moderate anemia: 8.0 – 10.9 g/dL
- Severe anemia: < 8.0 g/dL

**Respond ONLY with valid JSON in this exact format:**
```json
{
  "hemoglobin_estimate": 11.5,
  "stage": "MILD",
  "confidence": 0.75,
  "explanation": "Overall assessment explaining the findings across ALL 5 images",
  "per_image_findings": {
    "face": "Description of facial pallor indicators",
    "tongue": "Description of tongue color findings",
    "conjunctiva": "Description of eyelid/conjunctiva findings",
    "palm": "Description of palm/wrist findings",
    "nails": "Description of nail bed findings"
  },
  "ayurvedic_insights": {
    "dosha_assessment": "Ayurvedic dosha imbalance assessment (Vata/Pitta/Kapha) based on visible signs",
    "dietary_recommendations": "Iron-rich foods and Ayurvedic dietary advice (e.g., beetroot, pomegranate, dates, amla, jaggery, green leafy vegetables, black sesame)",
    "herbal_remedies": "Recommended Ayurvedic herbs and formulations (e.g., Punarnava, Mandoor Bhasma, Loha Bhasma, Ashwagandha, Shatavari, Triphala)",
    "lifestyle_tips": "Ayurvedic lifestyle recommendations (pranayama, yoga asanas, daily routines)",
    "home_remedies": "Simple home remedies (e.g., soaked raisins, amla juice with honey, beetroot-carrot juice, moringa leaves)"
  }
}
```

**Important:**
- **CRITICAL:** If the images DO NOT contain human body parts (face, tongue, eye, hand) or are irrelevant (objects, scenes), SET "stage" to "INVALID".
- stage must be one of: NORMAL, MILD, MODERATE, SEVERE, INVALID
- hemoglobin_estimate must be a float between 5.0 and 18.0
- confidence must be a float between 0.0 and 1.0
- Be conservative in your estimates - when uncertain, lean toward recommending medical consultation
- Provide specific and actionable Ayurvedic recommendations based on the observed severity
- This is a screening tool only, always recommend professional medical followup
- If patient details are provided, factor in age, gender, ethnicity, diet, and medical history for more accurate assessment

Analyze the provided images now:
        """.trimIndent()
    }

    /**
     * Builds the patient context section of the prompt from patient details.
     */
    private fun buildPatientContext(details: PatientDetails): String {
        val lines = mutableListOf<String>()
        lines.add("")
        lines.add("**PATIENT INFORMATION (use this for more accurate analysis):**")

        if (details.name.isNotBlank()) lines.add("- Name: ${details.name}")
        if (details.age > 0) lines.add("- Age: ${details.age} years")
        if (details.gender.isNotBlank()) lines.add("- Gender: ${details.gender}")
        if (details.ethnicity.isNotBlank()) lines.add("- Ethnicity: ${details.ethnicity}")
        if (details.region.isNotBlank()) lines.add("- Region: ${details.region}")
        if (details.weight > 0) lines.add("- Weight: ${details.weight} kg")
        if (details.dietType.isNotBlank()) lines.add("- Diet: ${details.dietType}")
        if (details.knownConditions.isNotBlank()) lines.add("- Known Medical Conditions: ${details.knownConditions}")
        if (details.currentSymptoms.isNotBlank()) lines.add("- Current Symptoms: ${details.currentSymptoms}")
        if (details.gender == "Female" && details.menstrualHistory.isNotBlank()) {
            lines.add("- Menstrual Status: ${details.menstrualHistory}")
        }
        if (details.previousAnemia) lines.add("- Previous Anemia Diagnosis: Yes")
        if (details.currentMedications.isNotBlank()) lines.add("- Current Medications: ${details.currentMedications}")

        lines.add("")
        lines.add("**Consider the above patient details when:**")
        lines.add("- Adjusting hemoglobin thresholds (e.g., gender-specific WHO ranges)")
        lines.add("- Interpreting skin pallor relative to ethnicity and natural skin tone")
        lines.add("- Assessing dietary risk factors (vegetarian diets are higher risk for iron-deficiency)")
        lines.add("- Correlating visible signs with reported symptoms")
        lines.add("- Tailoring Ayurvedic recommendations to the patient's constitution and region")
        lines.add("")

        return lines.joinToString("\n")
    }
}
