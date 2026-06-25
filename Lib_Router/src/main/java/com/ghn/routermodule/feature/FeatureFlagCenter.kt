package com.ghn.routermodule.feature

import com.ghn.routermodule.routerServiceOrNull

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
 * 描述: 功能开关中心，负责提供运行时开关读取与更新能力。
 */

object FeatureFlagCenter {
    fun isEnabled(featureKey: String, defaultEnabled: Boolean = true): Boolean {
        val provider = routerServiceOrNull<FeatureGateService>()
        return provider?.isEnabled(featureKey, defaultEnabled)
            ?: FeatureFlagStore.isEnabled(featureKey, defaultEnabled)
    }
}
