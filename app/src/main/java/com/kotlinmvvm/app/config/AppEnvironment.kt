package com.kotlinmvvm.app.config

/**
 * @author 浩楠
 * @date 2026/7/21 13:35
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用运行环境类型，构建参数只选择环境而不直接保存任何 BaseUrl
 */
enum class AppEnvironment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION;

    companion object {
        /** 将外部环境名称转换为稳定枚举，非法配置在应用装配前立即失败。 */
        fun from(value: String): AppEnvironment = when (value.trim().lowercase()) {
            "development", "dev" -> DEVELOPMENT
            "staging", "stage" -> STAGING
            "production", "prod" -> PRODUCTION
            else -> error("不支持的 APP_ENVIRONMENT: $value")
        }
    }
}
