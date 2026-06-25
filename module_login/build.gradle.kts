import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
}

android {
    (this as LibraryExtension).namespace = "com.ghn.module_login"
    configureAndroid()
    buildFeatures {
        viewBinding = true
    }
}

configureKotlinJvm()

ksp {
    arg("KOIN_DEFAULT_MODULE", "false")
}

dependencies {
    implementation(project(":Lib_Base"))
    implementation(libs.koin.annotations)
    ksp(libs.apt)
    ksp(libs.koin.ksp.compiler)
}
