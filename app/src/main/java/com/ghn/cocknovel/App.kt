package com.ghn.cocknovel

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process

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
