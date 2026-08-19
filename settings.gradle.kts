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
    }
}

rootProject.name = "KotlinMvvm"

// Shared Core
include(":core_model")
include(":core_state")
include(":core_navigation")
include(":core_ui")

// Data
include(":domain-feed")
include(":core_network")
include(":core_data")

// Playback
include(":core_playback")
include(":core_player")

// Features
include(":feature_home")
include(":feature_media")

// Platform Hosts
include(":app")
include(":shared_ios")
