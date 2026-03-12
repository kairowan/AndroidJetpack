package com.kt.network.net

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Legacy 网络错误枚举
 */
enum class ERROR(private val code: Int, private val err: String) {
    UNKNOWN(1000, "未知错误"),
    PARSE_ERROR(1001, "解析错误"),
    NETWORD_ERROR(1002, "网络错误"),
    HTTP_ERROR(1003, "协议出错"),
    TOKEN_EMPTY(-2, "No message available"),
    SSL_ERROR(1004, "证书出错"),
    NOT_FOUND(404, "not found"),
    TIMEOUT_ERROR(1006, "连接超时");

    fun getValue(): String = err

    fun getKey(): Int = code
}
