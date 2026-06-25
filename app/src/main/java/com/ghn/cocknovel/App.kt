package com.ghn.cocknovel

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.example.basemodel.base.BaseApplication
import com.kt.ktmvvm.lib.BuildConfig
import com.therouter.TheRouter


/**
 * @author 浩楠
 *
 * @date 2023/1/9   18:00.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述:
 */
open class App : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        if (isMainProcess()) {
            instance = this
        }
    }

    @SuppressLint("WrongConstant", "NewApi")
    fun isMainProcess(): Boolean {
        val pid = Process.myPid()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processName = activityManager.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName
        return processName == packageName
    }


    override fun attachBaseContext(base: Context?) {
        TheRouter.isDebug = BuildConfig.DEBUG
        super.attachBaseContext(base)
        instance = this
    }

    companion object {
        private lateinit var instance: App

        fun get(): App = instance
        fun context(): Context = instance.applicationContext
    }
}
