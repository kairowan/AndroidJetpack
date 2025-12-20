enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url=uri("https://maven.aliyun.com/repository/public") }
        maven { url=uri("https:/https://maven.aliyun.com/repository/google") }
        maven { url=uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url=uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url=uri("https://maven.aliyun.com/repository/public") }
        maven { url=uri("https://maven.aliyun.com/repository/google") }
        maven { url=uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven {
            setUrl("http://maven.aliyun.com/nexus/content/repositories/releases/")
            isAllowInsecureProtocol = true
        }
        maven { setUrl("https://jitpack.io") }
    }
}
rootProject.name = "KotlinMvvm"
include(":app")
include(":NetworkModule")
include(":BaseModule")
include(":CapturePacketModule")
include(":CommonModule")
include(":EventModule")
include(":RouterModule")


