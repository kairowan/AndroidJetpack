import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
}

buildscript {
    repositories {
        google()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    }
}

apply(plugin = "org.jetbrains.kotlin.jvm")

group = "com.kotlinmvvm.buildlogic"

repositories {
    google()
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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
    implementation("com.android.tools.build:gradle:8.9.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.21")
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
