plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.yagnesh.myhomeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yagnesh.myhomeapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/INDEX.LIST"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.generativeai)

    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // Work Manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.8.2")

    // Core KTX
    implementation("androidx.core:core-ktx:1.12.0")

    // Material 3
    implementation("androidx.compose.material3:material3:1.1.2")

    // Firestore
    implementation("com.google.firebase:firebase-firestore-ktx:24.9.1")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")

    // Coil for image loading and display
    implementation("io.coil-kt:coil-compose:2.5.0")

    // OkHttp for file upload
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Google Drive API
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // OkHttp for API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON parsing
    implementation("org.json:json:20230227")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}