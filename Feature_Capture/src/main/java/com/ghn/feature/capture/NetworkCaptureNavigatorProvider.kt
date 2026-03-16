package com.ghn.feature.capture

import com.ghn.routermodule.NetworkCaptureRouter
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.navigate
import com.therouter.inject.ServiceProvider
import com.therouter.inject.Singleton

@Singleton
@ServiceProvider(returnType = NetworkCaptureRouter::class)
class NetworkCaptureNavigatorProvider : NetworkCaptureRouter {
    override fun openNetworkCapture() {
        RouterPath.Net.NETWORKCAPTURE.navigate()
    }
}
