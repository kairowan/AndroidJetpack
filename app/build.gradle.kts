plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
    id("android.aop")
}
android {

    namespace = "com.ghn.cocknovel"
    configureAndroid()
    defaultConfig {
        applicationId = "com.ghn.cocknovel"
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }
//    signingConfigs {
//        create("platform") {
//            //将系统签名文件platform.keystore 放在projectName/app/ 目录下
//            storeFile = file("Cocknovel.jks")
//            storePassword = "Cocknovel" // 对应-srcstorepass
//            keyAlias = "Cocknovel" //对应-name
//            keyPassword = "Cocknovel" // 对应-pass
////            isV1SigningEnabled = true
////            isV2SigningEnabled = true
//        }
//    }
    buildTypes {
//        val signConfig = signingConfigs.getByName("platform")
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
//            signingConfig = signingConfigs.getByName("platform")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"

            )
//            signingConfig = signingConfigs.getByName("platform")
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

configureKotlinJvm()

androidAopConfig {
    include(
        "com.ghn.cocknovel",
        "com.ghn.feature.capture",
        "com.ghn.module_login",
        "com.ghn.routermodule"
    )
    exclude("kotlin.jvm", "kotlin.internal", "kotlinx.coroutines.internal", "kotlinx.coroutines.android")
    verifyLeafExtends = false
}

ksp {
    arg("KOIN_DEFAULT_MODULE", "false")
}

dependencies {
    implementation(project(":Lib_Base"))
    implementation(project(":Feature_Capture"))
    implementation(project(":module_login"))
    implementation(libs.koin.annotations)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.jessyan.autosize)
    implementation(libs.github.jsBridge)
    ksp(libs.apt)
    ksp(libs.koin.ksp.compiler)
}
