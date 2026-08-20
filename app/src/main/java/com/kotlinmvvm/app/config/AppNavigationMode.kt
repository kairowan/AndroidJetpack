package com.kotlinmvvm.app.config

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用页面承载模式，明确区分 Navigation 3 单 Activity 与独立页面多 Activity
 */
enum class AppNavigationMode {
    SINGLE_ACTIVITY,
    MULTI_ACTIVITY;

    companion object {
        /** 将构建参数解析为明确宿主模式，非法值直接阻止应用继续装配。 */
        fun from(value: String): AppNavigationMode = when (value.trim().lowercase()) {
            "single_activity", "single" -> SINGLE_ACTIVITY
            "multi_activity", "multi" -> MULTI_ACTIVITY
            else -> error("不支持的 APP_NAVIGATION_MODE: $value")
        }
    }
}
