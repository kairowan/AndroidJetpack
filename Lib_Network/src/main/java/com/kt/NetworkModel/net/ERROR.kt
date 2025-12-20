package com.kt.network.net

/**
 * @author 浩楠
 *
 * @date 2023/3/8   15:12.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 错误的枚举类
 */
enum class ERROR(private val code: Int, private val err: String) {

    /**
     * 未知错误
     */
    UNKNOWN(1000, "未知错误"),

    /**
     * 解析错误
     */
    PARSE_ERROR(1001, "解析错误"),

    /**
     * 网络错误
     */
    NETWORD_ERROR(1002, "网络错误"),

    /**
     * 协议出错
     */
    HTTP_ERROR(1003, "协议出错"),
    TOKEN_EMPTY(-2, "No message available"),

    /**
     * 证书出错
     */
    SSL_ERROR(1004, "证书出错"),
    NOT_FOUND(404, "not found"),

    /**
     * 连接超时
     */
    TIMEOUT_ERROR(1006, "连接超时");

    fun getValue(): String {
        return err
    }

    fun getKey(): Int {
        return code
    }

}