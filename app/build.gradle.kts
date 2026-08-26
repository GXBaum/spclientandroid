plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)

    // Navigation Compose
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.ksp)

    alias(libs.plugins.aboutlibraries.plugin.android)

    alias(libs.plugins.koin.compiler)
}


android {
    namespace = "de.rafaelbeckmann.hvkclient"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.rafaelbeckmann.hvkclient"
        minSdk = 29
        targetSdk = 36
        versionCode = 24
        versionName = "1.0.1-alpha.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // bei fehlern regeln in proguard.pro anpassen
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")

            // TODO: hat nicht funktioniert
            ndk.debugSymbolLevel = "FULL" // oder "SYMBOL_TABLE" // Play Store Warnung "This app bundle contains native code, and you've not uploaded debug symbols."
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    //implementation(libs.androidx.material3)
    implementation(libs.material3)
    implementation(libs.androidx.compose.animation)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    //Retrofit
    implementation(libs.okhttp)
    implementation(libs.okhttp.urlconnection)


    // TODO: maybe change to debugImplementation
    implementation(libs.logging.interceptor)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation Compose
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // data store
    implementation(libs.androidx.datastore.preferences)

    // icons
    implementation(libs.androidx.material.icons.extended)

    // Room
    implementation(libs.androidx.room)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.ksp)

    // für SplashScreen (Theme.Material3.DayNight.NoActionBar)
    implementation(libs.material)

    // splash screen compat for API <30
    implementation(libs.androidx.core.splashscreen)

    // AboutLibraries
    implementation(libs.aboutlibraries.compose.m3)

    // Work manager
    implementation(libs.androidx.work.runtime.ktx)

    // Jsoup (HTML parsing)
    implementation(libs.jsoup)

    implementation(libs.kotlinx.datetime)

    implementation(libs.bundles.ktor)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.koin.core)
    implementation(libs.koin.annotations)  // For annotation support
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.compose.viewmodel.navigation)
    implementation(libs.koin.androidx.workmanager)

}