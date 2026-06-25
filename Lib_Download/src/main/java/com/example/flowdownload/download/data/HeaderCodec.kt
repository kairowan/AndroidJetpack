package com.example.flowdownload.download.data

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 请求头序列化工具，负责在持久化层安全保存和恢复自定义 HTTP Header。
 */
object HeaderCodec {
    fun encode(headers: Map<String, String>): String = StringMapCodec.encode(headers)

    fun decode(encoded: String): Map<String, String> = StringMapCodec.decode(encoded)
}
