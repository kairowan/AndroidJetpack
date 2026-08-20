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

include(":lib-core-data")
include(":lib-core-designsystem")
include(":lib-core-network")
include(":lib-core-player")
include(":lib-core-ui")

include(":module-data-feed")
include(":module-domain-feed")

include(":module-feature-detail")
include(":module-feature-home")
include(":module-feature-shorts")
