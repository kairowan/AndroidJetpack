import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-kapt")
}
kapt {
    arguments {
        arg("THEROUTER_MODULE_NAME", project.name)
    }
}
android {
    (this as LibraryExtension).namespace = "com.ghn.feature.capture"
    configureAndroid()
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    kapt(libs.apt)
    implementation(project(":Lib_Base"))

}
