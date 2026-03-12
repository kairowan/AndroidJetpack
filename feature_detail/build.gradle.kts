plugins {
    alias(libs.plugins.kotlinmvvm.android.feature)
}

android {
    namespace = "com.kotlinmvvm.feature.detail"
}

dependencies {
    implementation(project(":core_ui"))
    implementation(project(":core_data"))
    implementation(project(":core_model"))
    implementation(project(":core_player"))
    implementation(project(":feature_media_shared"))
    
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
}
