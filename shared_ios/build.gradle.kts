plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    val frameworkBaseName = "SharedIosApp"
    val iosTargets = listOf(
        iosX64(),
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
        commonMain.dependencies {
            implementation(project(":core_data"))
            implementation(project(":core_design_tokens"))
            implementation(project(":core_model"))
            implementation(project(":core_ui_contract"))
            implementation(project(":feature_home_shared"))
            implementation(project(":feature_media_shared"))
            implementation(libs.kotlinx.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.core)
        }
    }
}
