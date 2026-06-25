package com.example.flowdownload.download.model

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 时间提供器抽象，方便下载流程在测试和生产环境中复用统一时钟能力。
 */
fun interface Clock {
    fun now(): Long
}

/**
 * 系统默认时钟实现，用于生产环境下的下载进度、恢复和状态持久化时间戳。
 */
object SystemClock : Clock {
    override fun now(): Long = System.currentTimeMillis()
}
