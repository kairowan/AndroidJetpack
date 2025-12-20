package com.ghn.routermodule


/**
 * @author 浩楠
 * @date 2025/5/30 17:09
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 接口定义，便于做服务解耦
 */
interface RouterService {
    fun goToHome()
    fun geToKey()
    fun goToProfile(userId: String)
    fun goToNet()
    fun goTo(path: String, extras: Map<String, Any?> = emptyMap())
}