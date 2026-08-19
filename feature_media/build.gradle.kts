@file:OptIn(
    org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class,
    org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class
)

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    iosArm64()
    iosSimulatorArm64()
    jvm("desktop")
    wasmJs {
        browser()
    }

    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core_model"))
            implementation(project(":core_state"))
            implementation(project(":core_ui"))
            implementation(project(":domain-feed"))
            implementation(libs.compose.multiplatform.runtime)
            implementation(libs.compose.multiplatform.foundation)
            implementation(libs.compose.multiplatform.material3)
            implementation(libs.compose.multiplatform.ui)
            implementation(libs.kotlinx.core)
        }
        androidMain.dependencies {
            implementation(project(":core_player"))
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.compose.material.iconsExtended)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewModelCompose)
            implementation(libs.coil.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.kotlinmvvm.feature.media"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
