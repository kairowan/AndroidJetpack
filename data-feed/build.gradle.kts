plugins {
    alias(libs.plugins.kotlinmvvm.android.library)
}

android {
    namespace = "com.kotlinmvvm.data.feed"
    defaultConfig.consumerProguardFiles("consumer-rules.pro")
}

dependencies {
    api(project(":domain-feed"))
    api(project(":core-network"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
}
