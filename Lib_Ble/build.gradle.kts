import com.android.build.api.dsl.LibraryExtension
plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
}

android {
    (this as LibraryExtension).namespace = "no.nordicsemi.android.ble"
    configureAndroid()
}

dependencies {

}
