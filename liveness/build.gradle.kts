plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")

}

android {
    namespace = "tech.sourceid.liveness"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"  // Add this for Kotlin 1.9.24
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }


    publishing {
        singleVariant("debug") {
            withSourcesJar()
            withJavadocJar()
        }
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}


// Publishing configuration - afterEvaluate is necessary for Android libraries
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.sourceidtechorg"
                artifactId = "sid-liveness-sdk-android"
                version = "1.8.1"

                pom {
                    name.set("SIDLiveness")
                    description.set("A SourceID native Android library for performing liveness check for KYC.")
                    url.set("https://github.com/sourceidtechorg/sid-liveness-sdk-android")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("richard-sid")
                            name.set("Richard Uzor")
                            email.set("richard@sourceid.tech")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/sourceidtechorg/sid-liveness-sdk-android.git")
                        developerConnection.set("scm:git:ssh://github.com/sourceidtechorg/sid-liveness-sdk-android.git")
                        url.set("https://github.com/sourceidtechorg/sid-liveness-sdk-android")
                    }
                }
            }
        }
    }
}


dependencies {
    // Support for Java 8 features
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // FaceLivenessDetector dependency
    implementation("com.amplifyframework.ui:liveness:1.6.0")

    // Amplify Auth dependency
    implementation("com.amplifyframework:aws-auth-cognito:2.29.2")

    // Material3 dependency for theming FaceLivenessDetector
    implementation("androidx.compose.material3:material3:1.3.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}