plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)

    //alias(libs.plugins.hilt.android) // TODO: error weil es schon in classpath im anderen build.gradle ist
    id("dagger.hilt.android.plugin")

    // Navigation Compose
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.ksp)

    alias(libs.plugins.aboutlibraries.plugin.android)
}


android {
    namespace = "de.rafaelbeckmann.hvkclient"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.rafaelbeckmann.hvkclient"
        minSdk = 29
        targetSdk = 36
        versionCode = 23
        versionName = "1.0.0-beta.1"

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
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp.urlconnection)


    implementation(libs.moshi.kotlin)

    // TODO: maybe change to debugImplementation
    implementation(libs.logging.interceptor)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //Dagger - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

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
    implementation(libs.androidx.hilt.work)

    // Jsoup (HTML parsing)
    implementation(libs.jsoup)
}