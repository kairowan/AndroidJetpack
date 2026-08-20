plugins {
    alias(libs.plugins.kotlinmvvm.android.feature)
}

android {
    namespace = "com.kotlinmvvm.feature.home"
}

dependencies {
    implementation(project(":lib-core-designsystem"))
    implementation(project(":lib-core-ui"))
    implementation(project(":module-domain-feed"))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    testImplementation(libs.kotlinx.coroutines.test)
}
