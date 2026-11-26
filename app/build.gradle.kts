plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "tech.sourceid.sdktesters"
    compileSdk = 36 // stick to stable LTS unless you need 36 preview

    defaultConfig {
        applicationId = "tech.sourceid.sdktesters"
        minSdk = 24
        targetSdk = 34
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"  // Add this for Kotlin 1.9.24

    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}


dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
//    implementation("com.amplifyframework:core:2.29.2")
//    implementation("com.amplifyframework:aws-auth-cognito:2.29.2")
//    implementation("com.amplifyframework:aws-api:2.29.2")
// Liveness SDK: keep at 1.6.0, but ensure you include core 2.29.2 explicitly
//    implementation("com.amplifyframework.ui:liveness:1.6.0")


    // FaceLivenessDetector dependency
//    implementation("com.amplifyframework.ui:liveness:1.6.0")

    // Amplify Auth dependency
//    implementation("com.amplifyframework:aws-auth-cognito:2.29.2")

    // Material3 dependency for theming FaceLivenessDetector
    implementation("androidx.compose.material3:material3:1.3.1")

    // Support for Java 8 features
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

//    implementation(project(":liveness"))
//    implementation("com.github.EQua-Dev:liveness-expo:v1.7.2")
    implementation("com.github.sourceidtechorg:sid-liveness-sdk-android:v1.8.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}