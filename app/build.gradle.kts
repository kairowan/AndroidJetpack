plugins {
    alias(libs.plugins.kotlinmvvm.android.application.compose)
    alias(libs.plugins.kotlin.serialization)
}

val navigationMode = providers.gradleProperty("APP_NAVIGATION_MODE")
    .orElse("single_activity")
    .get()
require(navigationMode in setOf("single_activity", "multi_activity")) {
    "APP_NAVIGATION_MODE 仅支持 single_activity 或 multi_activity，当前值: $navigationMode"
}

val appEnvironment = providers.gradleProperty("APP_ENVIRONMENT")
    .orElse("production")
    .get()
require(appEnvironment in setOf("development", "dev", "staging", "stage", "production", "prod")) {
    "APP_ENVIRONMENT 仅支持 development、staging 或 production，当前值: $appEnvironment"
}
val normalizedAppEnvironment = when (appEnvironment) {
    "development", "dev" -> "development"
    "staging", "stage" -> "staging"
    else -> "production"
}

android {
    namespace = "com.kotlinmvvm.app"
    defaultConfig {
        applicationId = "com.kotlinmvvm.compose.scaffold"
        testInstrumentationRunner = "com.kotlinmvvm.app.testing.AppTestRunner"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        buildConfigField("String", "APP_NAVIGATION_MODE", "\"$navigationMode\"")
        buildConfigField("String", "APP_ENVIRONMENT", "\"production\"")
    }

    buildFeatures.buildConfig = true

    buildTypes {
        getByName("release") {
            buildConfigField("String", "APP_ENVIRONMENT", "\"production\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        getByName("debug") {
            buildConfigField("String", "APP_ENVIRONMENT", "\"$normalizedAppEnvironment\"")
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":module-feature-home"))
    implementation(project(":module-feature-detail"))
    implementation(project(":module-feature-shorts"))
    implementation(project(":lib-core-designsystem"))
    implementation(project(":lib-core-network"))
    implementation(project(":lib-core-player"))
    implementation(project(":lib-core-ui"))
    implementation(project(":module-data-feed"))
    implementation(project(":module-domain-feed"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewModelNavigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.coil.compose)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
