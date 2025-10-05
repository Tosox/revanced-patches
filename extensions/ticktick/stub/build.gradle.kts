plugins {
    id(libs.plugins.android.library.get().pluginId)
}

android {
    namespace = "de.tosox.revanced.extension"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}