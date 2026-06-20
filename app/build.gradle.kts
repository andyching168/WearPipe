/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlinx.serialization)
}

android {
    namespace = "net.wearpipe.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.wearpipe.app"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/wearMain/AndroidManifest.xml")
            java.setSrcDirs(listOf("src/wearMain/kotlin"))
            kotlin.setSrcDirs(listOf("src/wearMain/kotlin"))
            res.setSrcDirs(listOf("src/wearMain/res"))
        }
        getByName("test") {
            java.setSrcDirs(listOf("src/wearTest/kotlin"))
            kotlin.setSrcDirs(listOf("src/wearTest/kotlin"))
        }
        getByName("androidTest") {
            java.setSrcDirs(listOf("src/wearAndroidTest/kotlin"))
            kotlin.setSrcDirs(listOf("src/wearAndroidTest/kotlin"))
        }
        getByName("debug") {
            manifest.srcFile("src/wearDebug/AndroidManifest.xml")
            java.setSrcDirs(listOf("src/wearDebug/kotlin"))
            kotlin.setSrcDirs(listOf("src/wearDebug/kotlin"))
            res.setSrcDirs(emptyList<String>())
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions.unitTests.isIncludeAndroidResources = true
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas-wear")
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.compose.ui:ui:1.11.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.11.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.11.2")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.wear.compose:compose-material3:1.6.2")
    implementation("androidx.wear.compose:compose-foundation:1.6.2")
    implementation("androidx.wear.compose:compose-navigation3:1.6.2")
    implementation("androidx.navigation3:navigation3-runtime:1.1.1")
    implementation("androidx.navigation3:navigation3-ui:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    implementation("androidx.media3:media3-exoplayer:1.10.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.10.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.10.0")
    implementation("androidx.media3:media3-session:1.10.0")
    implementation("androidx.media3:media3-ui:1.10.0")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("io.coil-kt.coil3:coil-compose:3.5.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")
    implementation(libs.newpipe.extractor)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("androidx.room:room-testing:2.8.4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.11.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.11.2")
}
