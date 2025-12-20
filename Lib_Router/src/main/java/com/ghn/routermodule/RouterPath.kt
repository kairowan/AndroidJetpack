package com.ghn.routermodule


/**
 * @author 浩楠
 * @date 2025/5/30 17:09
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO 统一管理所有路径常量
 */
object RouterPath {
    object Main {
        const val HOME = "/home/home"
    }

    object User {
        const val UserKEY = "/user/key"
    }

    object Web {
        const val WEBVIEW = "/web/browser"
    }

    object Net{
        const val NETWORKCAPTURE = "/net/NetworkCapture"
    }

    object Login {
        const val LoginAC = "/login/loginac"
    }
}