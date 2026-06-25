@file:JvmName("RouterMap__TheRouter__835833385")
package a

/**
 * Generated code, Don't modify!!!
 * Created by kymjs, and KSP Version is 1.3.2.
 * JDK Version is 17.0.17.
 */
@androidx.annotation.Keep
class RouterMap__TheRouter__835833385 : com.therouter.router.IRouterMapAPT {

	override fun init() { RouterMap__TheRouter__835833385.addRoute() }

	companion object { 

	const val TAG = "Created by kymjs, and KSP Version is 1.3.2."
	const val THEROUTER_APT_VERSION = "1.3.2"
	const val ROUTERMAP0 = "[{\"path\":\"/net/NetworkCapture\",\"className\":\"com.ghn.feature.capture.ui.activity.NetworkCaptureActivity\",\"action\":\"\",\"description\":\"\",\"params\":{}}]"
	const val COUNT = 1

	@JvmStatic
	fun addRoute() {
		val item1 = com.therouter.router.RouteItem("/net/NetworkCapture","com.ghn.feature.capture.ui.activity.NetworkCaptureActivity","","")
		com.therouter.router.addRouteItem(item1)
	}
	}
}
