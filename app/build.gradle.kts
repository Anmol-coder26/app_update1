import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        load(localFile.inputStream())
    }
}

android {
    namespace = "com.guardian.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.guardian.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        val geminiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")

        val agoraAppId = localProperties.getProperty("AGORA_APP_ID") ?: ""
        buildConfigField("String", "AGORA_APP_ID", "\"$agoraAppId\"")
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
        jniLibs {
            pickFirsts.addAll(listOf(
                "**/libaosl.so",
                "**/libc++_shared.so",
                "lib/**/libaosl.so",
                "lib/arm64-v8a/libaosl.so",
                "lib/armeabi-v7a/libaosl.so",
                "lib/x86/libaosl.so",
                "lib/x86_64/libaosl.so"
            ))
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    // Agora Voice & Audio RTC SDK
    implementation("io.agora.rtc:voice-sdk:4.4.1")
    // Agora RTM (Signaling) SDK for Real-Time Transcripts
    implementation("io.agora.rtm:rtm-sdk:2.2.1")

    // CameraX & ML Kit for QR / Barcode Scanner
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Encrypted Session Storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Room Database & Coroutines for Number Reputation & Call History
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20231013")
}