import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("android")
    alias(libs.plugins.ksp)
}

android {
    (this as LibraryExtension).namespace = "com.ghn.routermodule"
    configureAndroid()
}

configureKotlinJvm()

dependencies {
    api(libs.router)
    api(libs.androidaop.annotation)

    implementation(project(":Lib_Utils"))
    implementation(project(":Lib_UI_Common"))

    ksp(libs.androidaop.apt)
}
