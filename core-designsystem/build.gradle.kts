plugins {
    alias(libs.plugins.kotlinmvvm.android.library.compose)
}

android {
    namespace = "com.kotlinmvvm.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
}
