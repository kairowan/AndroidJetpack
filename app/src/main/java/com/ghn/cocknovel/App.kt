package com.ghn.cocknovel

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process

/**
 * @author 浩楠
 *
 * @date 2026-2-20
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (isMainProcess()) {
            instance = this
            com.kt.network.net.RetrofitClient.getInstance(this)
        }
    }

    @SuppressLint("WrongConstant", "NewApi")
    fun isMainProcess(): Boolean {
        val pid = Process.myPid()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processName = activityManager.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName
        return processName == packageName
    }

    companion object {
        private lateinit var instance: App
        fun get(): App = instance
        fun context(): Context = instance.applicationContext
    }
}
