plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "es.cleantv.tv"
    compileSdk = 35
    defaultConfig {
        applicationId = "es.cleantv.tv"
        minSdk = 26
        targetSdk = 28
        versionCode = 2
        versionName = "0.7.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    // Varias dependencias transitivas contienen metadatos de licencia con el mismo nombre.
    // Solo se excluyen copias de estos recursos del APK; se conservan las licencias
    // del codigo fuente y el archivo LICENSE del proyecto.
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/NOTICE.md"
            )
        }
    }
}
dependencies {
    implementation("dev.mobile:dadb:1.2.6")
}
