import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    kotlin("android")
}

android {
    (this as LibraryExtension).namespace = "com.kt.ktmvvm.lib"
    configureAndroid()
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }


}

dependencies {
    api(project(":core_legacy_network"))
    api(libs.okhttp.okhttp4.logging)
    api(libs.okhttp.okhttp4)
    api(libs.retrofit.retrofit2)
    api(libs.retrofit.retrofit2.gson)
    api(libs.retrofit.retrofit2.scalars)
    api(libs.jetbrains.annotations)
    api(libs.aliyun.httpdns)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_1_9)
        apiVersion.set(KotlinVersion.KOTLIN_1_9)
    }
}
