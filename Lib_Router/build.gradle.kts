import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("kotlin-android")

    kotlin("android")
}

android {
    (this as LibraryExtension).namespace = "com.ghn.routermodule"
    configureAndroid()
}

dependencies {
    api(libs.router)
}