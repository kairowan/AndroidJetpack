import com.android.build.api.dsl.LibraryExtension

plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
}

android {
    (this as LibraryExtension).namespace = "com.ghn.eventmodule"
    configureAndroid()
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
    implementation(libs.lifecycle.runtime)
}