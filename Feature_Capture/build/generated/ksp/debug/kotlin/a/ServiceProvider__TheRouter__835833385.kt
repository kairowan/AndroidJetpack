@file:JvmName("ServiceProvider__TheRouter__835833385")
package a

/**
 * Generated code, Don't modify!!!
 * Created by kymjs, and KSP Version is 1.3.2.
 * JDK Version is 17.0.17.
 */
@androidx.annotation.Keep
public class ServiceProvider__TheRouter__835833385 : com.therouter.inject.Interceptor {

	override fun initFlowTask(context: android.content.Context, digraph: com.therouter.flow.Digraph) {
		ServiceProvider__TheRouter__835833385.addFlowTask(context, digraph)
	}

	override fun <T> interception(clazz: Class<T>?, vararg params: Any?): T? {
		var obj: T? = null
		if (com.ghn.routermodule.NetworkCaptureRouter::class.java.equals(clazz) && params.size == 0) {
			val returnType: com.ghn.routermodule.NetworkCaptureRouter = com.ghn.feature.capture.NetworkCaptureNavigatorProvider()
			obj = returnType as T?
		} else if (com.ghn.routermodule.CaptureBridgeWebRouter::class.java.equals(clazz) && params.size == 0) {
			val returnType: com.ghn.routermodule.CaptureBridgeWebRouter = com.ghn.feature.capture.web.CaptureBridgeWebNavigatorProvider()
			obj = returnType as T?
		} else {

        }
        return obj
    }

	companion object { 

	const val TAG = "Created by kymjs, and KSP Version is 1.3.2."
	const val THEROUTER_APT_VERSION = "1.3.2"
	const val FLOW_TASK_JSON = "{}"

		@kotlin.jvm.JvmStatic
		fun addFlowTask(context: android.content.Context, digraph: com.therouter.flow.Digraph) {
		}
	}
}
