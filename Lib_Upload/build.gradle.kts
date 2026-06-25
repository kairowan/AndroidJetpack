import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
}

android {
    (this as LibraryExtension).namespace = "com.ghn.lib.upload"
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
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation(libs.okhttp.okhttp4)
    implementation(libs.kotlinx.android)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    ksp(libs.apt)
    ksp(libs.koin.ksp.compiler)
}
