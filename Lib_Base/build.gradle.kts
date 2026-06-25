import com.android.build.api.dsl.LibraryExtension

plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
}
android {
    (this as LibraryExtension).namespace ="com.ghn.lib.base"
    configureAndroid()
}

configureKotlinJvm()

dependencies {
    api(project(":Lib_Ble"))
    api(project(":Lib_Utils"))
    api(project(":Lib_Event"))
    api(project(":Lib_Router"))
    api(project(":Lib_Network"))
    api(project(":Lib_UI_Common"))
    api(project(":Lib_Download"))
    api(project(":Lib_Upload"))
    api(libs.androidaop.annotation)
    api(libs.androidaop.extra)
    api(libs.androidx.room.ktx)
    api(libs.androidx.room.runtime)
    api(libs.koin.android)
    ksp(libs.androidaop.apt)
}
