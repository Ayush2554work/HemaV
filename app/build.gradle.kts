import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

// Load API keys from secrets.properties (preferred) or local.properties (fallback)
val secretsProperties = Properties().apply {
    val secretsFile = rootProject.file("secrets.properties")
    val localPropsFile = rootProject.file("local.properties")
    if (secretsFile.exists()) {
        load(secretsFile.inputStream())
    } else if (localPropsFile.exists()) {
        load(localPropsFile.inputStream())
    }
}

android {
    namespace = "com.meditech.hemav"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.meditech.hemav"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-poc"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Inject API keys as BuildConfig fields
        buildConfigField("String", "GEMINI_API_KEY",
            "\"${secretsProperties.getProperty("GEMINI_API_KEY", "")}\"")
        buildConfigField("String", "GROQ_API_KEY",
            "\"${secretsProperties.getProperty("GROQ_API_KEY", "")}\"")
        buildConfigField("String", "HUGGINGFACE_API_KEY",
            "\"${secretsProperties.getProperty("HUGGINGFACE_API_KEY", "")}\"")
        buildConfigField("String", "SENTRY_DSN",
            "\"${secretsProperties.getProperty("SENTRY_DSN", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }



    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Firebase BOM
    val firebaseBom = platform("com.google.firebase:firebase-bom:33.1.2")
    implementation(firebaseBom)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Google AI SDK (Gemini)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // OkHttp for Groq & HuggingFace API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Guava for CameraX ListenableFuture
    implementation("com.google.guava:guava:31.1-android")

    // CameraX
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Razorpay Integration
    implementation("com.razorpay:checkout:1.6.38")

    // Jitsi Meet
    implementation("org.jitsi.react:jitsi-meet-sdk:10.1.0")

    // Sentry Crash Monitoring
    implementation("io.sentry:sentry-android:7.14.0")
}
