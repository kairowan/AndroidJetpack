package com.kt.NetworkModel.net.interceptor

/**
 * @author 浩楠
 *
 * @date 2023/5/12-13:36.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
enum class Level {
    /**
     * No logs.
     */
    NONE,

    /**
     *
     * Example:
     * <pre>`- URL
     * - Method
     * - Headers
     * - Body
    `</pre> *
     */
    BASIC,

    /**
     *
     * Example:
     * <pre>`- URL
     * - Method
     * - Headers
    `</pre> *
     */
    HEADERS,

    /**
     *
     * Example:
     * <pre>`- URL
     * - Method
     * - Body
    `</pre> *
     */
    BODY
}