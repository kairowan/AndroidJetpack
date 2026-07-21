plugins {
    alias(libs.plugins.kotlinmvvm.android.library)
}

android {
    namespace = "com.kotlinmvvm.core.network"
}

dependencies {
    api(libs.retrofit.core)
    implementation(libs.kotlinx.coroutines.core)
    api(libs.okhttp.core)
    implementation(libs.retrofit.gson)
    testImplementation(libs.okhttp.mockwebserver)
}
