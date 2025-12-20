package com.example.basemodel.base.basevm

import android.app.Activity
import android.os.Bundle


/**
 * @author 浩楠
 * @date 2025/5/29 14:30
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 页面跳转
 */
interface BaseViewModelNavigation : BaseViewModelLiveData {

    /**
     * 仅启动 Activity（无参数）
     */
    fun startActivity(clz: Class<out Activity>) {
        val params = hashMapOf<String, Any?>()
        params["CLASS"] = clz
        uc.getStartActivityEvent().postValue(params as Map<String, Any>)
    }

    /**
     * 启动 Activity，携带 Bundle 参数
     */
    fun startActivity(clz: Class<out Activity>, bundle: Bundle? = null) {
        val params = hashMapOf<String, Any?>()
        params["CLASS"] = clz
        params["BUNDLE"] = bundle
        uc.getStartActivityEvent().postValue(params as Map<String, Any>)
    }

    /**
     * 启动 Activity，携带 Bundle + RequestCode
     */
    fun startActivity(clz: Class<out Activity>, bundle: Bundle? = null, requestCode: Int) {
        val params = hashMapOf<String, Any?>()
        params["CLASS"] = clz
        params["BUNDLE"] = bundle
        params["REQUEST"] = requestCode
        uc.getStartActivityEvent().postValue(params as Map<String, Any>)
    }

    /**
     * 启动模块内 Activity（插件跳转）
     */
    fun startModelActivity(packageName: String?, className: String?) {
        val params = mutableMapOf<String, Any?>()
        params["CANONICAL_NAME"] = packageName
        params["CLASS"] = className
        uc.getStartModelActivityEvent().postValue(params as Map<String, Any>)
    }

    /**
     * 结束当前 Activity
     */
    fun finish() {
        uc.getFinishEvent().postValue(null)
    }

    /**
     * 模拟返回事件（onBackPressed）
     */
    fun onBackPressed() {
        uc.getOnBackPressedEvent().postValue(null)
    }
}