@Suppress(
    "DSL_SCOPE_VIOLATION"
) // ignore warnings here, it's an Android Studio bug: https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

detekt {
    toolVersion = "1.22.0"
    buildUponDefaultConfig = true
}
