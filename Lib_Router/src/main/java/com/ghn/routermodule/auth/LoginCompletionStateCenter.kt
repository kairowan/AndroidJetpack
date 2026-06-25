package com.ghn.routermodule.auth

import java.util.Collections
import java.util.WeakHashMap

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: LoginCompletionStateCenter 中心组件，负责统一管理相关状态或动作分发。
 */

object LoginCompletionStateCenter {

    private val completedTargets = Collections.newSetFromMap(WeakHashMap<Any, Boolean>())

    @Synchronized
    fun markCompleted(target: Any) {
        completedTargets.add(target)
    }

    @Synchronized
    fun consumeCompleted(target: Any): Boolean {
        return completedTargets.remove(target)
    }
}
