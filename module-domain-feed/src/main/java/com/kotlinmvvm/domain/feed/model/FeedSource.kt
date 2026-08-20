package com.kotlinmvvm.domain.feed.model

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 业务层可选择的信息流来源，不暴露具体接口路径
 */
enum class FeedSource(val diagnosticCode: String) {
    HOME_SELECTED("home_selected"),
    DISCOVERY("discovery"),
    FOLLOW("follow"),
    DISCOVERY_HOT("discovery_hot"),
    DISCOVERY_CATEGORY("discovery_category"),
    AUTHORS("authors")
}
