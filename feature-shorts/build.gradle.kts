plugins {
    alias(libs.plugins.kotlinmvvm.android.feature)
}

android {
    namespace = "com.kotlinmvvm.feature.shorts"
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":domain-feed"))
    implementation(project(":core-player"))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    testImplementation(libs.kotlinx.coroutines.test)
}
