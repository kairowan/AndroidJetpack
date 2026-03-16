plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
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
        dataBinding = true
    }
}

dependencies {
    implementation(project(":Lib_Base"))
    implementation(project(":Feature_Capture"))
    implementation(project(":module_login"))

//    implementation(libs.github.titlebar)
//    implementation(libs.github.xbanner)
//    implementation(libs.github.xxPermissions)
//
//    implementation(libs.androidx.navigation.fragment)
//    implementation(libs.androidx.navigation.ui)
    kapt(libs.apt)
}
