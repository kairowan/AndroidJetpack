import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.ksp)
}

android {
    (this as LibraryExtension).namespace = "com.ghn.feature.capture"
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
    ksp(libs.apt)
    implementation(project(":Lib_Base"))
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp.compiler)

}
