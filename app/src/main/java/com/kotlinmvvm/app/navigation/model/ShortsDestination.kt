package com.kotlinmvvm.app.navigation.model

import kotlinx.serialization.Serializable

/**
 * @author 浩楠
 * @date 2026/7/20 15:26
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 短视频顶层导航目的地，作为短视频独立返回栈的固定根节点
 */
@Serializable
data object ShortsDestination : AppRoute
