plugins {
    alias(libs.plugins.kotlinmvvm.android.library.compose)
}

android {
    namespace = "com.kotlinmvvm.core.player"
}

dependencies {
    implementation(project(":core_ui"))
    api(project(":core_playback"))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui.compose)
    implementation(libs.media3.datasource)
    implementation(libs.media3.database)
}
