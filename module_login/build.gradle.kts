import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
    id("kotlin-kapt")
}

android {
    (this as LibraryExtension).namespace = "com.ghn.module_login"
    configureAndroid()
    buildFeatures {
        viewBinding = true
    }
}

ksp {
    arg("KOIN_DEFAULT_MODULE", "false")
}

dependencies {
    implementation(project(":Lib_Base"))
    implementation(libs.koin.annotations)
    kapt(libs.apt)
    ksp(libs.koin.ksp.compiler)
}
