buildscript {
    dependencies {
        classpath ("com.meituan.android.walle:plugin:1.1.7")
    }
}
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("io.github.flyjingfish.androidaop") version "2.6.9"
}
true
