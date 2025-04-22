plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    kotlin("kapt")
    id ("kotlin-parcelize")
}

android {
    namespace = "com.example.travel_mientrung"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.travel_mientrung"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

// Firebase Authentication
    implementation ("com.google.firebase:firebase-auth-ktx:22.1.2")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")  // Cho Google Sign-In (nếu cần)

    implementation ("com.google.firebase:firebase-firestore-ktx:24.10.0") // hoặc phiên bản mới nhất

    implementation ("androidx.cardview:cardview:1.0.0")
    // Facebook Login (nếu cần)
    implementation("com.facebook.android:facebook-login:latest.release")
    implementation ("com.squareup.picasso:picasso:2.71828") // hoặc Glide

    implementation ("com.google.firebase:firebase-database-ktx:20.3.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1") // Hoặc phiên bản mới nhất
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1") // Hoặc phiên bản tương ứng với Glide

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.squareup.picasso:picasso:2.71828")
    // Glide core library
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    // Glide compiler (needed for @GlideModule)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

}