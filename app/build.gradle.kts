plugins {

    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.sneha.wesafe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sneha.wesafe"
        minSdk = 26
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx:24.7.1")
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation ("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor ("com.github.bumptech.glide:compiler:5.0.5")

    val room_version = "2.6.1" // latest stable

    implementation ("androidx.room:room-runtime:$room_version")
    annotationProcessor ("androidx.room:room-compiler:$room_version") // for Java

    // Optional - Room with LiveData
    implementation ("androidx.room:room-ktx:$room_version")
}