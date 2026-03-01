plugins {
    alias(libs.plugins.kotlinmvvm.android.library)
}

android {
    namespace = "com.kotlinmvvm.core.data"
}

dependencies {
    api(project(":core_model"))
    api(project(":Lib_Network"))
    implementation(libs.kotlinx.core)
}
