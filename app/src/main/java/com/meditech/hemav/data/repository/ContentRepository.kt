package com.meditech.hemav.data.repository

data class WellnessContent(
    val id: String,
    val title: String,
    val description: String,
    val type: ContentType,
    val benefit: String,
    val instructions: List<String> = emptyList()
)

enum class ContentType {
    YOGA,
    AYURVEDIC_TIP
}

class ContentRepository {
    private val contentList = listOf(
        WellnessContent(
            id = "yoga_1",
            title = "Surya Namaskar",
            description = "Sun Salutation - A complete workout for the physical and spiritual body.",
            type = ContentType.YOGA,
            benefit = "Improves blood circulation and flexibility.",
            instructions = listOf("Stand at the front of your mat", "Inhale and lift your arms", "Exhale and fold forward")
        ),
        WellnessContent(
            id = "yoga_2",
            title = "Vrikshasana",
            description = "Tree Pose - Find your balance and focus.",
            type = ContentType.YOGA,
            benefit = "Strengthens legs and improves concentration.",
            instructions = listOf("Stand tall", "Place one foot on the opposite inner thigh", "Bring hands to heart center")
        ),
        WellnessContent(
            id = "tip_1",
            title = "Morning Warm Water",
            description = "Drink a glass of warm water first thing in the morning with a pinch of salt/honey.",
            type = ContentType.AYURVEDIC_TIP,
            benefit = "Kindles the Agni (digestive fire) and flushes toxins (Ama)."
        ),
        WellnessContent(
            id = "tip_2",
            title = "The Pomegranate Power",
            description = "Pomegranate juice is considered one of the best 'Rakta Vardhak' (Blood Builders) in Ayurveda.",
            type = ContentType.AYURVEDIC_TIP,
            benefit = "Naturally boosts hemoglobin and Pitta balance."
        ),
        WellnessContent(
            id = "yoga_3",
            title = "Anulom Vilom",
            description = "Alternate Nostril Breathing - The king of pranayamas.",
            type = ContentType.YOGA,
            benefit = "Purifies the Nadis (energy channels) and reduces stress.",
            instructions = listOf("Sit in Padmasana", "Close right nostril with thumb", "Inhale through left", "Switch and exhale through right")
        ),
        WellnessContent(
            id = "tip_3",
            title = "Avoid Cold Water with Meals",
            description = "Sipping ice-cold water during meals can dampen your digestive fire.",
            type = ContentType.AYURVEDIC_TIP,
            benefit = "Ensures proper nutrient absorption and prevents bloating."
        ),
        WellnessContent(
            id = "tip_4",
            title = "Early Dinner Rule",
            description = "Finish your last meal at least 3 hours before sleep.",
            type = ContentType.AYURVEDIC_TIP,
            benefit = "Improves sleep quality and metabolic health."
        )
    )

    fun getAllContent() = contentList
    fun getYogaPoses() = contentList.filter { it.type == ContentType.YOGA }
    fun getDailyTips() = contentList.filter { it.type == ContentType.AYURVEDIC_TIP }
}
