plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    val frameworkBaseName = "SharedIosApp"
    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = frameworkBaseName
            isStatic = true
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation(project(":core_data"))
            implementation(project(":core_designsystem"))
            implementation(project(":core_model"))
            implementation(project(":domain-feed"))
            implementation(project(":feature_detail"))
            implementation(project(":feature_home"))
            implementation(project(":feature_home_shared"))
            implementation(project(":feature_media_shared"))
            implementation(project(":feature_shorts"))
            implementation(project(":shared-ui"))
            implementation(libs.compose.multiplatform.foundation)
            implementation(libs.compose.multiplatform.material3)
            implementation(libs.compose.multiplatform.runtime)
            implementation(libs.compose.multiplatform.ui)
            implementation(libs.kotlinx.core)
        }
    }
}
