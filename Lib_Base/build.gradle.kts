import com.android.build.api.dsl.LibraryExtension

plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
    kotlin("kapt")
}
android {
    (this as LibraryExtension).namespace ="com.ghn.lib.base"
    configureAndroid()
}

dependencies {
    api(project(":Lib_Ble"))
    api(project(":Lib_Utils"))
    api(project(":Lib_Event"))
    api(project(":Lib_Router"))
    api(project(":Lib_Network"))
    api(project(":Lib_UI_Common"))

    api(libs.androidx.room.ktx)
    api(libs.androidx.room.runtime)
    api(libs.rxlifecycle.rxlifecycle4.android)
    api(libs.rxlifecycle.rxlifecycle4.components)
}