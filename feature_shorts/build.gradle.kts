plugins {
    alias(libs.plugins.kotlinmvvm.android.feature)
}

android {
    namespace = "com.kotlinmvvm.feature.shorts"
}

dependencies {
    implementation(project(":core_ui"))
    implementation(project(":core_data"))
    implementation(project(":core_model"))
    implementation(project(":core_player"))
    
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
}
