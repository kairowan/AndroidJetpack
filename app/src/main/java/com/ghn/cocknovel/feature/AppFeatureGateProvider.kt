package com.ghn.cocknovel.feature

import com.ghn.routermodule.feature.FeatureFlagStore
import com.ghn.routermodule.feature.FeatureGateService
import com.therouter.inject.ServiceProvider
import com.therouter.inject.Singleton

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
 * 描述: 应用侧功能门控提供者，实现业务开关状态的对接逻辑。
 */

@Singleton
@ServiceProvider(returnType = FeatureGateService::class)
class AppFeatureGateProvider : FeatureGateService {
    override fun isEnabled(featureKey: String, defaultEnabled: Boolean): Boolean {
        return FeatureFlagStore.isEnabled(featureKey, defaultEnabled)
    }
}
