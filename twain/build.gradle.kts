import java.net.URI

@Suppress(
    "DSL_SCOPE_VIOLATION"
) // ignore warnings here, it's an Android Studio bug: https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
    id("signing")
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
            isMinifyEnabled = true
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

val version = project.properties["VERSION_NAME"].toString()
val group = project.properties["GROUP"].toString()

fun isReleaseBuild(): Boolean {
    return !project.properties["VERSION_NAME"].toString().contains("SNAPSHOT")
}

fun getReleaseRepositoryUrl(): URI {
    val url =
        if (project.hasProperty("RELEASE_REPOSITORY_URL")) project.properties["RELEASE_REPOSITORY_URL"].toString()
        else "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
    return URI(url)
}

fun getSnapshotRepositoryUrl(): URI {
    val url =
        if (project.hasProperty("SNAPSHOT_REPOSITORY_URL")) project.properties["SNAPSHOT_REPOSITORY_URL"].toString()
        else "https://s01.oss.sonatype.org/content/repositories/snapshots/"
    return URI(url)
}

fun getRepositoryUsername(): String {
    return if (project.hasProperty("NEXUS_USERNAME")) project.properties["NEXUS_USERNAME"].toString()
    else error("Needs username for repository")
}

fun getRepositoryPassword(): String {
    return if (project.hasProperty("NEXUS_PASSWORD")) project.properties["NEXUS_PASSWORD"].toString()
    else error("Needs password for repository")
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl =
                project.properties["RELEASE_REPOSITORY_URL"]?.let { URI(it.toString()) }
                    ?: error("Release repository not defined")
            val snapshotsRepoUrl =
                project.properties["SNAPSHOT_REPOSITORY_URL"]?.let { URI(it.toString()) }
                    ?: error("Snapshot repository not defined")
            url = (if (isReleaseBuild()) releasesRepoUrl else snapshotsRepoUrl)

            credentials {
                username = getRepositoryUsername()
                password = getRepositoryPassword()
            }
        }
    }
    publications {
        register<MavenPublication>("twain") {
            pom {
                name.set(project.properties["POM_NAME"].toString())
                description.set(project.properties["POM_DESCRIPTION"].toString())
                url.set(project.properties["POM_URL"].toString())
                groupId = project.properties["GROUP_ID"].toString()
                artifactId = project.properties["ARTIFACT_ID"].toString()
                version = project.properties["VERSION_NAME"].toString()

                licenses {
                    license {
                        name.set(project.properties["POM_LICENCE_NAME"].toString())
                        url.set(project.properties["POM_LICENCE_URL"].toString())
                        distribution.set(project.properties["POM_LICENCE_DIST"].toString())
                    }
                }

                developers {
                    developer {
                        id.set(project.properties["POM_DEVELOPER_ID"].toString())
                        name.set(project.properties["POM_DEVELOPER_NAME"].toString())
                        email.set(project.properties["POM_DEVELOPER_EMAIL"].toString())
                    }
                }

                scm {
                    connection.set(project.properties["POM_SCM_CONNECTION"].toString())
                    developerConnection.set(project.properties["POM_SCM_DEV_CONNECTION"].toString())
                    url.set(project.properties["POM_SCM_URL"].toString())
                }
            }
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

signing {
    sign(publishing.publications["twain"])
}

dependencies {
    implementation(libs.bundles.markwon)
    implementation(libs.bundles.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.coil)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.core)
}
