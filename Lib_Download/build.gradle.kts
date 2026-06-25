import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
}

android {
    (this as LibraryExtension).namespace = "com.ghn.lib.download"
    configureAndroid()

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

configureKotlinJvm()

ksp {
    arg("KOIN_DEFAULT_MODULE", "false")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation(libs.okhttp.okhttp4)
    implementation(libs.retrofit.retrofit2)
    implementation(libs.kotlinx.android)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    ksp(libs.apt)
    ksp(libs.koin.ksp.compiler)
    ksp("androidx.room:room-compiler:2.6.1")
}
