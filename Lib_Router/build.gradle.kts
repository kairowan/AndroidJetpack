import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    kotlin("android")
}

android {
    (this as LibraryExtension).namespace = "com.ghn.routermodule"
    configureAndroid()
}

dependencies {
    api(libs.router)
}