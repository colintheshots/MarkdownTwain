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
