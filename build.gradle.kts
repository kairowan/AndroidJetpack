buildscript {
    dependencies {
        classpath ("com.meituan.android.walle:plugin:1.1.7")
    }
}
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
//    alias(libs.plugins.androidApplication) apply false
//    alias(libs.plugins.androidLibrary) apply false
//    alias(libs.plugins.kotlinAndroid) apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
}
true
