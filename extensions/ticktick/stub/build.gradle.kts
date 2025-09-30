plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.tosox.revanced.extension"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
}