import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlinJvm)
}

group = "com.kotlinmvvm.buildlogic"

repositories {
    google()
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(
        fileTree(gradle.gradleHomeDir!!.resolve("lib")) {
            include("gradle-kotlin-dsl-*.jar")
            include("gradle-kotlin-dsl-extensions-*.jar")
            include("gradle-kotlin-dsl-shared-runtime-*.jar")
            include("gradle-kotlin-dsl-tooling-models-*.jar")
        }
    )
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.compiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "kotlinmvvm.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "kotlinmvvm.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "kotlinmvvm.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "kotlinmvvm.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "kotlinmvvm.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
    }
}
