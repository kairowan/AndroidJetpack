package org.koin.ksp.generated

import org.koin.core.module.Module
import org.koin.dsl.*


public val com_ghn_feature_capture_di_CaptureModule : Module get() = module {
	single() { _ -> com.ghn.feature.capture.web.CaptureWebBridgeModule() } bind(com.ghn.lib.base.web.bridge.WebBridgeModule::class)
}
public val com.ghn.feature.capture.di.CaptureModule.module : org.koin.core.module.Module get() = com_ghn_feature_capture_di_CaptureModule
