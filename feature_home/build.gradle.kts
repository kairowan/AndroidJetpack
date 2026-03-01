plugins {
    alias(libs.plugins.kotlinmvvm.android.feature)
}

android {
    namespace = "com.kotlinmvvm.feature.home"
}

dependencies {
    api(project(":core_model"))
    api(project(":core_data"))
}
