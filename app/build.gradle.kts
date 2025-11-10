plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.regenx"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.regenx"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            // The fallback should be a string containing a double quote,
            // which is why we use an inner \" instead of just ""
            "\"${project.findProperty("GEMINI_API_KEY") ?: "UNDEFINED"}\""
        )
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}


dependencies {
    // --- Compose UI dependencies ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)


    // --- General Android/Compose dependencies (Manual libs preserved) ---
    implementation(libs.androidx.activity.compose.v181)
    implementation(libs.material3)
    implementation(libs.play.services.location)
    implementation(libs.ui)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.scenecore)

    // REMOVED: implementation(libs.firebase.functions.ktx) - We'll use the hardcoded one below or the BOM

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Firebase BOM and Core Dependencies (Simplified and Standardized) ---
    // Use the highest version BOM to control all Firebase library versions
    implementation(platform(libs.firebase.bom))
    // REMOVED: implementation(platform(libs.firebase.bom.v33150))

    // Hardcoded BOM for version stability (33.15.0)
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    // Core Firebase libraries - No versions needed if BOM is used!
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.google.firebase.messaging.ktx)

    // 🌟 FIX: Use the KTX dependency without a version, relying on the BOM. 🌟
    implementation("com.google.firebase:firebase-functions-ktx")

    implementation("com.google.firebase:firebase-storage-ktx") // No version

    // --- Other KTX/Hilt/External Dependencies ---
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material3)

    // ViewModel + Fragment KTX
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v270)
    implementation(libs.androidx.fragment.ktx.v162)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.fragment)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.browser)
    implementation("androidx.browser:browser:1.3.0")


    //for icons in resident module
    implementation("androidx.compose.material:material-icons-extended")

    // Location/Maps/Database (Kept hardcoded versions where necessary)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("com.google.maps.android:maps-compose:2.11.4")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.firebase:firebase-firestore:24.4.6") // Version conflicts handled by BOM
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

}