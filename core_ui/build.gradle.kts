plugins {
    alias(libs.plugins.kotlinmvvm.android.library.compose)
}

android {
    namespace = "com.kotlinmvvm.core.ui"
}

dependencies {
    api(project(":core_designsystem"))
    
    // ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.kotlinx.core)
}
