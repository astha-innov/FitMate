

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}



android {
    namespace = "com.fitmate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fitmate"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        val geminiApiKey =
            project.findProperty("GEMINI_API_KEY")?.toString() ?: ""

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"$geminiApiKey\""
        )

        vectorDrawables {
            useSupportLibrary = true
        }
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

        // IMPORTANT FIX
        isCoreLibraryDesugaringEnabled = true
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

    // =========================
    // COMPOSE BOM
    // =========================

    val composeBom =
        platform("androidx.compose:compose-bom:2024.09.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    // =========================
    // FIREBASE BOM
    // =========================

    val firebaseBom =
        platform("com.google.firebase:firebase-bom:34.7.0")

    implementation(firebaseBom)

    // =========================
    // CORE
    // =========================

    implementation("androidx.core:core-ktx:1.13.1")

    implementation("androidx.activity:activity-compose:1.9.2")

    // =========================
    // LIFECYCLE
    // =========================

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    // =========================
    // NAVIGATION
    // =========================

    implementation("androidx.navigation:navigation-compose:2.8.0")

    // =========================
    // COROUTINES
    // =========================

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // =========================
    // FIREBASE
    // =========================

    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // =========================
    // COIL
    // =========================

    implementation("io.coil-kt:coil-compose:2.7.0")

    // =========================
    // COMPOSE UI
    // =========================

    implementation("androidx.compose.ui:ui")

    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.compose.material3:material3")

    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.coil-kt:coil-gif:2.6.0")

    //grok

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // =========================
    // DESUGARING
    // =========================

    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs:2.0.4"
    )

    // =========================
    // TESTING
    // =========================

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")



    debugImplementation("androidx.compose.ui:ui-test-manifest")
}