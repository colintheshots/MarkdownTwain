import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
    id("signing")
}

android {
    namespace = "com.colintheshots.twain"
    compileSdk = 34

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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions.jvmTarget = "1.8"
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    coordinates(
        "com.colintheshots",
        "twain",
        libs.versions.twain.get()
    )

    pom {
        name = "Markdown Twain"
        description = "A Markdown editor for Jetpack Compose"
        url = "https://github.com/colintheshots/MarkdownTwain"
        inceptionYear = "2022"
        licenses {
            license {
                name = "The Apache Software License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "colintheshots"
                name = "Colin Lee"
                email = "colin@colintheshots.com"
            }
        }
        scm {
            connection = "scm:git@github.com:colintheshots/MarkdownTwain.git"
            developerConnection = "scm:git@github.com:colintheshots/MarkdownTwain.git"
            url = "https://github.com/colintheshots/MarkdownTwain"
        }
    }
    configure(AndroidSingleVariantLibrary("release", sourcesJar = true, publishJavadocJar = true))
}

signing {
    sign(publishing.publications)
}

dependencies {
    implementation(libs.bundles.markwon)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.core)
}
