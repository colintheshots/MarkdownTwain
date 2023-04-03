@Suppress(
    "DSL_SCOPE_VIOLATION"
) // ignore warnings here, it's an Android Studio bug: https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.detekt)
}

detekt {
    toolVersion = "1.22.0"
    config = files("detekt.yml")
    buildUponDefaultConfig = true
}
