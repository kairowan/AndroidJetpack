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
    // ponytail: Kotlin/Wasm 的 Node 安装任务会动态添加官方 Ivy 分发仓库。
    // 等 Kotlin 插件支持 settings 级 Node 仓库后恢复 FAIL_ON_PROJECT_REPOS。
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "KotlinMvvm"
include(":app")

// Core Modules
include(":core_designsystem")
include(":core_design_tokens")
include(":core_ui")
include(":core_ui_contract")
include(":core_state")
include(":shared_ios")
include(":shared-ui")
include(":core_model")
include(":core_data")
include(":domain-feed")
include(":core_navigation")
include(":core_playback")
include(":core_player")

// Feature Modules
include(":feature_home")
include(":feature_home_shared")
include(":feature_media_shared")
include(":feature_detail")
include(":feature_shorts")
