plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-kapt")
}

kapt {
    generateStubs = true
    useBuildCache = false
}

android {

    namespace = "com.ghn.cocknovel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ghn.cocknovel"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = 35
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.toString()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        externalNativeBuild {
//            cmake {
//                arguments("-DANDROID_STL=c++_shared", "-DCMAKE_SHARED_LINKER_FLAGS=-Wl,--hash-style=both")
//            }
//        }
//        ndk {
//            abiFilters.add("armeabi-v7a")
//            abiFilters.add("arm64-v8a")
//        }
    }
    signingConfigs {
        create("platform") {
            //将系统签名文件platform.keystore 放在projectName/app/ 目录下
            storeFile = file("Cocknovel.jks")
            storePassword = "Cocknovel" // 对应-srcstorepass
            keyAlias = "Cocknovel" //对应-name
            keyPassword = "Cocknovel" // 对应-pass
//            isV1SigningEnabled = true
//            isV2SigningEnabled = true
        }
    }
    buildTypes {
        val signConfig = signingConfigs.getByName("platform")
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signConfig
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"

            )
            signingConfig = signConfig
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
//    packagingOptions {
//        exclude("AndroidManifest.xml")
//    }
//    dexOptions {
//        javaMaxHeapSize = "4g"
//    }
}

dependencies {
//    implementation(project(":CommonModule"))
    implementation(project(":BaseModule"))
//    implementation(project(":NetworkModule"))
    implementation(project(":RouterModule"))
    implementation(project(":EventModule"))
    implementation(project(":CapturePacketModule"))

    implementation(libs.github.titlebar)
    implementation(libs.github.xbanner)
    implementation(libs.github.xxPermissions)

    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    kapt(libs.apt)
}

