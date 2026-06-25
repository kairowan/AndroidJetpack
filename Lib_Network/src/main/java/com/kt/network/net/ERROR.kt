package com.kt.network.net

enum class ERROR(private val code: Int, private val err: String) {

    UNKNOWN(1000, "未知错误"),
    PARSE_ERROR(1001, "解析错误"),
    NETWORK_ERROR(1002, "网络错误"),
    HTTP_ERROR(1003, "协议出错"),
    SSL_ERROR(1004, "证书出错"),
    TIMEOUT_ERROR(1005, "连接超时"),
    UNAUTHORIZED(1006, "认证失败"),
    BAD_REQUEST(1007, "请求错误"),
    NOT_FOUND(1008, "资源未找到");

    fun getValue(): String {
        return err
    }

    fun getKey(): Int {
        return code
    }
}
