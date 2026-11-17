import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.google.gms.google.services)
}

// Token público de Mapbox: se lee de local.properties, que no se versiona
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "edu.uniquindio.co.tribooo"
    compileSdk {
        // hilt-navigation-compose 1.4.0 exige compilar contra la API 37 (targetSdk sigue en 36)
        version = release(37)
    }

    defaultConfig {
        applicationId = "edu.uniquindio.co.tribooo"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "mapbox_access_token", localProperties.getProperty("MAPBOX_ACCESS_TOKEN", ""))
        // Client ID web (OAuth tipo 3) de Firebase para "Continuar con Google"; también se lee de local.properties
        resValue("string", "google_web_client_id", localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", ""))
        // API key de OpenRouter (asistente de moderación); también se lee de local.properties
        buildConfigField("String", "OPENROUTER_API_KEY", "\"${localProperties.getProperty("OPENROUTER_API_KEY", "")}\"")
        ndk { abiFilters += listOf("arm64-v8a", "x86_64") }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    // Firebase BoM: fija versiones compatibles de todas las librerías Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)

    // --- Inicio de sesión con Google (Credential Manager) ---
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)

    // --- Mapas (Mapbox) ---
    implementation(libs.mapbox.android)
    implementation(libs.mapbox.compose)

    // --- Hilt Core ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // --- Hilt + Compose Navigation ---
    implementation(libs.androidx.hilt.navigation.compose)

    // --- DataStore (sesión persistente) ---
    implementation(libs.data.store)

    // --- Splash screen (API 12+ con compatibilidad hacia atrás) ---
    implementation(libs.androidx.core.splashscreen)

    // --- Red (OpenRouter con Retrofit + kotlinx.serialization) ---
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}