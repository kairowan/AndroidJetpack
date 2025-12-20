import com.android.build.api.dsl.LibraryExtension
plugins {
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
}

android {
    (this as LibraryExtension).namespace = "com.kairowan.lib_ui_common"
    configureAndroid()
}

dependencies {
    api(libs.github.titlebar)
    api(libs.github.xbanner)
    api(libs.github.xxPermissions)

    api(libs.androidx.navigation.fragment)
    api(libs.androidx.navigation.ui)

    api(libs.google.material)
    api(libs.androidx.appcompat)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.coordinatorlayout)

    api(libs.github.glide)

    api(libs.jessyan.autosize)
    api(libs.refresh.header.classics)
    api(libs.github.xpopup)
    api(libs.dialog.avi.library)
    api(libs.dialog.blankj)
    api(libs.dialogs.lifecycle)
    api(libs.dialogs.core)
    api(libs.github.lqdbrv)

    api(libs.github.titlebar)
    api(libs.github.xbanner)
    api(libs.github.xxPermissions)

    api(libs.androidx.navigation.fragment)
    api(libs.androidx.navigation.ui)
}