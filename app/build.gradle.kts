plugins {
    alias(libs.plugins.kotlinmvvm.android.application.compose)
    id("org.jetbrains.kotlin.kapt")
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
    implementation(project(":Lib_Network"))
    implementation(project(":feature_home"))
    implementation(project(":feature_detail"))
    implementation(project(":feature_shorts"))
    implementation(project(":core_designsystem"))
    implementation(project(":core_ui"))
    implementation(project(":core_data"))
    implementation(project(":core_model"))
    
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.apt)
}

kapt {
    correctErrorTypes = true
}
