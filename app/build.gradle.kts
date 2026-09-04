plugins {
    // AGP 9+ has built-in Kotlin support, so no separate Kotlin plugin is needed
    // even though LoginActivity is written in Kotlin.
    alias(libs.plugins.android.application)
    // Google Services plugin: reads app/google-services.json and generates the
    // Firebase config resources the SDK needs at runtime.
    id("com.google.gms.google-services")
}

android {
    namespace = "com.gritacademy.draftlifecycle"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.gritacademy.draftlifecycle"
        minSdk = 24
        // Kept at 33 so the foreground service only needs FOREGROUND_SERVICE +
        // the POST_NOTIFICATIONS runtime permission (no API 34+ service-type rules).
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    // Firebase BoM: pins every Firebase library to one compatible version set,
    // so individual Firebase dependencies below are declared without a version.
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
}
