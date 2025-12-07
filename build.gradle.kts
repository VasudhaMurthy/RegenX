plugins {
    // Keep only the highest version for the Android Application plugin
    id("com.android.application") version "8.13.1" apply false

    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false

    // NEW: You MUST add this line for Kotlin 2.0 + Compose to work!
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}9