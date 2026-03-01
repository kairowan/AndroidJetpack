buildscript {
    dependencies {
        classpath("com.meituan.android.walle:plugin:1.1.7")
    }
}

plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
}
