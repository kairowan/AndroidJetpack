import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    (this as LibraryExtension).namespace = "com.kt.ktmvvm.lib"
    configureAndroid()
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }


}

configureKotlinJvm()

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.okhttp.okhttp4.logging)
    api(libs.okhttp.okhttp4)
    api(libs.retrofit.retrofit2)
    api(libs.retrofit.retrofit2.gson)
    api(libs.retrofit.retrofit2.scalars)
    api(libs.jetbrains.annotations)
    api(libs.aliyun.httpdns)
    api(libs.kotlinx.core)
}
