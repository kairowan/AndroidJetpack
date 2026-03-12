package com.kt.network.bean

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Legacy 网络层通用返回模型
 */
open class BaseResult<T> {
    var success: Boolean = false
    var errorCode: Int = 0
    var errorMsg: String = ""
    var data: T? = null

    fun errorCode(): Int = errorCode

    fun errorMsg(): String = errorMsg

    fun data(): T? = data

    fun isSuccess(): Boolean = errorCode == 0
}
