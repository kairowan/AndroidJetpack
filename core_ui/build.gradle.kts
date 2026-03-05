plugins {
    alias(libs.plugins.kotlinmvvm.android.library.compose)
}

android {
    namespace = "com.kotlinmvvm.core.ui"
}

dependencies {
    api(project(":core_designsystem"))
    api(libs.kotlinx.collections.immutable)
    
    // ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.kotlinx.core)
}
