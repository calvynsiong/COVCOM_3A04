plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.covcom"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.covcom"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // When using the BoM, you don't specify versions in Firebase library dependencies

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging:23.4.1")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.jakewharton:butterknife:10.2.3")
    implementation("com.firebaseui:firebase-ui-database:8.0.2")
    implementation("com.karumi:dexter:6.2.3")
//Scalable screen size
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    implementation("com.intuit.ssp:ssp-android:1.0.6")

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}