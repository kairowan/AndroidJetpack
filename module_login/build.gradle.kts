import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    (this as LibraryExtension).namespace = "com.ghn.module_login"
    configureAndroid()
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":Lib_Base"))
    kapt(libs.apt)
}