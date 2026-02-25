

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.kapt")
    id ("kotlin-parcelize")

}

android {
    namespace = "com.levent.project2002"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.levent.project2002"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures{
         viewBinding=true
    }
}

dependencies {

    // 1. Firebase BoM (Always the first Firebase dependency)
    // This manages all Firebase versions automatically.
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // 2. Androidx and Material Libraries (Versions handled by libs.toml or build system)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview) // Keep the original declaration
    implementation(libs.androidx.viewpager2)

    // 3. Firebase Dependencies (NO explicit versions needed here, BoM handles them)
    // Removed duplicates and fixed the primary declaration style.
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1") // Using KTX
    implementation("com.google.firebase:firebase-database-ktx:21.0.0") // 🔥 Changed to KTX for consistency
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4") // 🔥 This is the core fix
    implementation("com.google.firebase:firebase-storage:21.0.1")

    // 4. Google Play Services Dependencies (Maps & Location)
    // These must keep their explicit versions as they are not managed by the Firebase BoM.

    kapt("com.github.bumptech.glide:compiler:4.16.0")
    // 5. Other Libraries
    implementation("com.github.bumptech.glide:glide:4.16.0") // Explicit version needed
    implementation("com.google.code.gson:gson:2.13.2")       // Explicit version needed
    implementation("com.tbuonomo:dotsindicator:5.1.0")        // Explicit version needed
    implementation("org.osmdroid:osmdroid-android:6.1.18")
// (Opsiyonel ama önerilen) Harita işaretçileri için kütüphane
    implementation("androidx.preference:preference-ktx:1.2.1")
    // 6. Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}