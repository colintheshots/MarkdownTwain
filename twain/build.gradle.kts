@Suppress(
    "DSL_SCOPE_VIOLATION"
) // ignore warnings here, it's an Android Studio bug: https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.meetup.twain"
    compileSdk = 33

    defaultConfig {
        aarMetadata {
            minCompileSdk = 24
        }
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    publishing {
        singleVariant("release")
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.meetup"
            artifactId = "twain"
            version = "1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation(libs.bundles.markwon)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.coil)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.core)
}
