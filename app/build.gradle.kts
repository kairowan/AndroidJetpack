plugins {
    alias(libs.plugins.kotlinmvvm.android.application.compose)
}

android {
    namespace = "com.ghn.cocknovel"
    defaultConfig {
        applicationId = "com.ghn.cocknovel"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":feature_home"))
    implementation(project(":feature_media"))
    implementation(project(":core_ui"))
    implementation(project(":core_network"))
    implementation(project(":core_data"))
    implementation(project(":domain-feed"))
    implementation(project(":core_model"))
    implementation(project(":core_navigation"))
    implementation(project(":core_player"))
    
    implementation(libs.google.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewModelNavigation3)

    testImplementation(libs.test.junit)
}
