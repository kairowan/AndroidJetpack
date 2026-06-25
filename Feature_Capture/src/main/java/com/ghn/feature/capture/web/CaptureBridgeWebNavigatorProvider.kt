package com.ghn.feature.capture.web

import com.ghn.routermodule.CaptureBridgeWebRouter
import com.therouter.inject.ServiceProvider
import com.therouter.inject.Singleton

@Singleton
@ServiceProvider(returnType = CaptureBridgeWebRouter::class)
class CaptureBridgeWebNavigatorProvider : CaptureBridgeWebRouter
