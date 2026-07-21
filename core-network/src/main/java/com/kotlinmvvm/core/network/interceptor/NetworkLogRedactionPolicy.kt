package com.kotlinmvvm.core.network.interceptor

/**
 * @author 浩楠
 * @date 2026/7/21 14:17
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 网络日志统一脱敏策略，确保请求、响应、地址和请求头使用同一组敏感字段规则
 */
internal object NetworkLogRedactionPolicy {

    fun isSensitiveHeader(name: String): Boolean = normalize(name) in SENSITIVE_HEADERS ||
        isSensitiveName(name)

    fun isSensitiveName(name: String): Boolean {
        val normalized = normalize(name)
        return normalized in SENSITIVE_NAMES || normalized.endsWith("token") ||
            normalized.contains("password") || normalized.endsWith("secret") ||
            normalized.endsWith("apikey") || normalized.contains("credential") ||
            normalized.contains("privatekey") || normalized.contains("signature") ||
            normalized.startsWith("session")
    }

    private fun normalize(value: String) = value.lowercase().replace("-", "").replace("_", "")

    private val SENSITIVE_HEADERS = setOf(
        "authorization", "proxyauthorization", "cookie", "setcookie", "xapikey", "apikey"
    )
    private val SENSITIVE_NAMES = setOf(
        "password", "token", "accesstoken", "refreshtoken", "secret", "authorization",
        "cookie", "apikey", "jwt", "bearer", "credential", "clientsecret", "privatekey",
        "signature", "session", "sessionid", "otp", "pin"
    )
}
