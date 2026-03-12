enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven {
            setUrl("http://maven.aliyun.com/nexus/content/repositories/releases/")
            isAllowInsecureProtocol = true
        }
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "KotlinMvvm"
include(":app")
include(":Lib_Network")

// Core Modules
include(":core_designsystem")
include(":core_design_tokens")
include(":core_ui")
include(":core_ui_contract")
include(":core_state")
include(":core_legacy_network")
include(":shared_ios")
include(":core_model")
include(":core_data")
include(":core_navigation")
include(":core_playback")
include(":core_player")

// Feature Modules
include(":feature_home")
include(":feature_home_shared")
include(":feature_media_shared")
include(":feature_detail")
include(":feature_shorts")
