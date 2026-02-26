package com.meditech.hemav.data.repository

import com.meditech.hemav.data.model.Medicine
import java.util.UUID

data class StoreProduct(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val scientificName: String = "",
    val description: String,
    val category: String,
    val price: Double,
    val unit: String = "Bottle",
    val imageUrl: String = "",
    val benefits: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val stockCount: Int = 100
)

class MedicineRepository {
    private val products = listOf(
        // === BLOOD HEALTH ===
        StoreProduct(
            name = "HemaV Iron Plus",
            description = "Natural iron booster formulated with Amla and Mandur Bhasma for effective hemoglobin recovery.",
            category = "Blood Health",
            price = 499.0,
            benefits = listOf("Boosts Hemoglobin", "Combats Fatigue", "Purely Natural"),
            ingredients = listOf("Mandur Bhasma", "Amalaki", "Loha Bhasma")
        ),
        StoreProduct(
            name = "Punarnavadi Mandur",
            description = "Traditional Ayurvedic tablet for anemia and inflammatory conditions.",
            category = "Blood Health",
            price = 350.0,
            benefits = listOf("Supports RBC Production", "Liver Detox", "Natural Diuretic")
        ),
        StoreProduct(
            name = "Lohasava Liquid",
            description = "Potent Ayurvedic tonic for treating anemia and jaundice while improving digestion.",
            category = "Blood Health",
            price = 280.0,
            unit = "450ml Liquid",
            benefits = listOf("Fast Absorption", "Increased Energy", "Digestive Aid")
        ),
        StoreProduct(
            name = "Saptamrit Lauh",
            description = "Focuses on eye health and iron deficiency, balancing Pitta and Rakta.",
            category = "Blood Health",
            price = 220.0,
            benefits = listOf("Eye Health", "Iron Boost", "Anti-inflammatory")
        ),
        StoreProduct(
            name = "Dhatri Lauh",
            description = "Amla-based iron preparation that is light on the stomach and highly effective.",
            category = "Blood Health",
            price = 310.0,
            benefits = listOf("No Constipation", "Natural Vitamin C", "Hb Support")
        ),

        // === IMMUNITY ===
        StoreProduct(
            name = "Amrit Kalash Capsules",
            description = "Nectar of Immortality. Premium full-spectrum antioxidant and immunity booster.",
            category = "Immunity",
            price = 1250.0,
            benefits = listOf("Anti-Aging", "Mental Clarity", "Peak Immunity")
        ),
        StoreProduct(
            name = "Giloy Satva Pure",
            description = "Concentrated extract of Tinospora Cordifolia, known for fighting chronic fevers.",
            category = "Immunity",
            price = 180.0,
            benefits = listOf("Fever Support", "Liver Protection", "Immunity")
        ),
        StoreProduct(
            name = "Chyawanprash Special",
            description = "Classic Ayurvedic jam enriched with 40+ herbs and fresh Amla.",
            category = "Immunity",
            price = 450.0,
            unit = "500g Jar"
        ),
        StoreProduct(
            name = "Ashwagandha Extract",
            description = "Stress relief and vitality booster for modern-day exhaustion.",
            category = "Immunity",
            price = 399.0
        ),
        StoreProduct(
            name = "Tulsi Ghan Vati",
            description = "Pure Holy Basil extract for respiratory health and viral defense.",
            category = "Immunity",
            price = 150.0
        ),

        // === DIGESTION ===
        StoreProduct(
            name = "Triphala Churna",
            description = "The classic digestive cleanser combining Three Fruits.",
            category = "Digestion",
            price = 120.0,
            unit = "100g Powder"
        ),
        StoreProduct(
            name = "Avipattikar Churna",
            description = "Best for hyperacidity, bloating, and Pitta-related digestive issues.",
            category = "Digestion",
            price = 160.0
        ),
        StoreProduct(
            name = "Hingwashtak Churna",
            description = "Spicy blend to kindle digestive fire (Agni) and remove gas.",
            category = "Digestion",
            price = 140.0
        ),
        StoreProduct(
            name = "Liver Care (Liv-52 Type)",
            description = "Supports liver function and protects against toxins.",
            category = "Digestion",
            price = 260.0
        ),
        StoreProduct(
            name = "Amla Juice Premium",
            description = "Cold-pressed fresh Amla juice for Vitamin C and cooling digestion.",
            category = "Digestion",
            price = 199.0,
            unit = "500ml Bottle"
        ),

        // === WOMEN'S HEALTH ===
        StoreProduct(
            name = "Shatavari Extract",
            description = "The 'Woman of 100 Husbands'. Balances hormones and supports lactation.",
            category = "Women's Health",
            price = 420.0
        ),
        StoreProduct(
            name = "Ashokarishta Liquid",
            description = "Uterine tonic for regular cycles and pelvic health.",
            category = "Women's Health",
            price = 240.0
        ),
        StoreProduct(
            name = "Pushyanuga Churna",
            description = "Specialized powder for gynecological disorders.",
            category = "Women's Health",
            price = 180.0
        ),
        StoreProduct(
            name = "Chandraprabha Vati",
            description = "Supports urinary tract and reproductive health for both genders.",
            category = "Women's Health",
            price = 290.0
        ),

        // === MENTAL WELLNESS ===
        StoreProduct(
            name = "Brahmi Vati Gold",
            description = "Memory booster and focus enhancer with real gold Bhasma.",
            category = "Mental Wellness",
            price = 850.0
        ),
        StoreProduct(
            name = "Medha Vati",
            description = "Balances the mind, reduces anxiety, and improves sleep quality.",
            category = "Mental Wellness",
            price = 320.0
        ),
        StoreProduct(
            name = "Tagar (Indian Valerian)",
            description = "Natural sleep aid for those with Vata-induced insomnia.",
            category = "Mental Wellness",
            price = 270.0
        ),
        StoreProduct(
            name = "Shankhpushpi Syrup",
            description = "Excellent brain tonic for students and professionals.",
            category = "Mental Wellness",
            price = 185.0
        ),

        // === PAIN RELIEF ===
        StoreProduct(
            name = "Mahanarayan Taila",
            description = "Warm Sesame oil with 50+ herbs for joint and muscle pain.",
            category = "Pain Relief",
            price = 450.0,
            unit = "100ml Oil"
        ),
        StoreProduct(
            name = "Yograj Guggulu",
            description = "Traditional Vata-balancing tablet for arthritis and stiffness.",
            category = "Pain Relief",
            price = 310.0
        ),
        StoreProduct(
            name = "Rhumaveda Spray",
            description = "Fast-acting Ayurvedic pain relief spray for sports injuries.",
            category = "Pain Relief",
            price = 225.0
        ),
        StoreProduct(
            name = "Orthopedic Balms",
            description = "Intense cooling and heating action for chronic back pain.",
            category = "Pain Relief",
            price = 99.0
        ),

        // === SKIN & HAIR ===
        StoreProduct(
            name = "Kumkumadi Oil Premium",
            description = "Saffron-based nighttime facial serum for radiant skin.",
            category = "Skin & Hair",
            price = 999.0,
            unit = "12ml Bottle"
        ),
        StoreProduct(
            name = "Neem & Turmeric Face Wash",
            description = "Antibacterial cleansing for acne-prone skin.",
            category = "Skin & Hair",
            price = 250.0
        ),
        StoreProduct(
            name = "Bhringraj Hair Oil",
            description = "The 'King of Hair'. Stops hair fall and promotes new growth.",
            category = "Skin & Hair",
            price = 380.0
        ),
        StoreProduct(
            name = "Manjistha Powder",
            description = "Blood purifier that results in glowing clear skin.",
            category = "Skin & Hair",
            price = 145.0
        ),

        // ... Adding more to reach around 50 ...
        StoreProduct(name = "Khadirarishta", description = "For skin allergies.", category = "Skin & Hair", price = 240.0),
        StoreProduct(name = "Mahamanjisthadi", description = "Deep blood detox.", category = "Blood Health", price = 350.0),
        StoreProduct(name = "Arjunarishta", description = "Heart health tonic.", category = "Immunity", price = 280.0),
        StoreProduct(name = "Gokshuradi Guggulu", description = "Kidney & UTI support.", category = "Women's Health", price = 260.0),
        StoreProduct(name = "Kaishore Guggulu", description = "Gout and blood purity.", category = "Pain Relief", price = 290.0),
        StoreProduct(name = "Kanchanar Guggulu", description = "Glandular health.", category = "Immunity", price = 310.0),
        StoreProduct(name = "Trikatu Churna", description = "Metabolic fire.", category = "Digestion", price = 110.0),
        StoreProduct(name = "Sitopaladi Churna", description = "Cough & Cold.", category = "Immunity", price = 130.0),
        StoreProduct(name = "Talishadi Churna", description = "Respiratory support.", category = "Immunity", price = 140.0),
        StoreProduct(name = "Vasant Kusumakar Ras", description = "Revitalizer.", category = "Immunity", price = 1600.0),
        StoreProduct(name = "Swarna Bhasma Pure", description = "Ancient vitality.", category = "Immunity", price = 4500.0),
        StoreProduct(name = "Honey (Wild Forest)", description = "Pure raw honey.", category = "Digestion", price = 350.0),
        StoreProduct(name = "Shilajit Resin", description = "Energy & Vitality.", category = "Immunity", price = 799.0),
        StoreProduct(name = "Guduchi Extract", description = "Universal healer.", category = "Immunity", price = 250.0),
        StoreProduct(name = "Haridra (Turmeric)", description = "Anti-inflammatory.", category = "Skin & Hair", price = 180.0),
        StoreProduct(name = "Karela Jamun Juice", description = "Diabetes support.", category = "Digestion", price = 220.0),
        StoreProduct(name = "Mahasudarshan Vati", description = "Universal febrifuge.", category = "Immunity", price = 190.0),
        StoreProduct(name = "Vasaka Syrup", description = "Lung health.", category = "Immunity", price = 160.0),
        StoreProduct(name = "Mustakarishta", description = "Digestive tonic.", category = "Digestion", price = 230.0),
        StoreProduct(name = "Abhayarishta", description = "Bowel health.", category = "Digestion", price = 210.0)
    )

    fun getProducts() = products
    fun getProductsByCategory(category: String) = products.filter { it.category == category }
    fun getProductById(id: String) = products.find { it.id == id }
    fun searchProducts(query: String) = products.filter { 
        it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) 
    }
}
