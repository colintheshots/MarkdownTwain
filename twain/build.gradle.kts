import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties

@Suppress(
    "DSL_SCOPE_VIOLATION"
) // ignore warnings here, it's an Android Studio bug: https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("com.vanniktech.maven.publish") version "0.28.0"
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
            isMinifyEnabled = true
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

fun getLocalProperty(key: String, file: String = "local.properties"): Any {
    val properties = Properties()
    val localProperties = File(file)
    if (localProperties.isFile) {
        InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    }

    return properties.getProperty(key) ?: ""
}

val repoUserName = getLocalProperty("nexus_username").toString()
val repoPassword = getLocalProperty("nexus_password").toString()
mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    coordinates(
        "com.colintheshots",
        "twain",
        "0.2.3"
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
