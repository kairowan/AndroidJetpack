plugins {
    alias(libs.plugins.kotlinmvvm.android.library.compose)
}

android {
    namespace = "com.kotlinmvvm.core.ui"
}

dependencies {
    api(project(":core_designsystem"))
    api(project(":core_ui_contract"))
    api(project(":core_state"))
    api(libs.kotlinx.collections.immutable)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.kotlinx.core)
}
