pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ComposeScaffold"
include(":app")

include(":core-data")
include(":core-designsystem")
include(":core-network")
include(":core-player")
include(":core-ui")

include(":data-feed")
include(":domain-feed")

include(":feature-detail")
include(":feature-home")
include(":feature-shorts")
