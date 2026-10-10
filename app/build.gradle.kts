plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.diego.dreambud"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.diego.dreambud"
        minSdk = 26
        targetSdk = 37
        // Overridable via Gradle properties, e.g. -PversionName=1.2.0 -PversionCode=42
        versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = (project.findProperty("versionName") as String?) ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("ci") {
            val keystorePath = System.getenv("DREAMBUD_KEYSTORE_FILE") ?: ""
            if (keystorePath.isNotEmpty()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("DREAMBUD_KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("DREAMBUD_KEY_ALIAS") ?: ""
                keyPassword = System.getenv("DREAMBUD_KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // CI release signing when a keystore is provided, debug keystore otherwise
            // so the APK is always installable.
            signingConfig = if (!System.getenv("DREAMBUD_KEYSTORE_FILE").isNullOrBlank()) {
                signingConfigs.getByName("ci")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.biometric)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.mediapipe.tasks.genai)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}