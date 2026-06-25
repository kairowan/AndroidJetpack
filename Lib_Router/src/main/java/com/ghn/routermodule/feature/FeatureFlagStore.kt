package com.ghn.routermodule.feature

import com.ghn.commonmodule.ext.MVUtils

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
 * 描述: 功能开关存储接口，负责抽象开关数据的持久化读写。
 */

object FeatureFlagStore {
    private const val PREFIX = "feature_flag:"

    fun isEnabled(featureKey: String, defaultEnabled: Boolean = true): Boolean {
        return MVUtils.getBoolean(keyOf(featureKey), defaultEnabled)
    }

    fun setEnabled(featureKey: String, enabled: Boolean) {
        MVUtils.put(keyOf(featureKey), enabled)
    }

    private fun keyOf(featureKey: String): String = "$PREFIX$featureKey"
}
