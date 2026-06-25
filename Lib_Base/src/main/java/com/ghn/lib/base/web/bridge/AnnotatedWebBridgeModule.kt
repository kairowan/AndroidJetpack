package com.ghn.lib.base.web.bridge

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

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
 * 描述: 基于注解的 WebBridge 模块基类，负责收集并注册桥接方法。
 */

abstract class AnnotatedWebBridgeModule : WebBridgeModule {

    final override fun handlerNames(): Set<String> {
        return BridgeMethodRegistry.methodsFor(javaClass)
            .mapTo(linkedSetOf()) { it.name }
    }

    final override fun register(registrar: WebBridgeRegistrar, host: WebBridgeHost) {
        BridgeMethodRegistry.methodsFor(javaClass).forEach { invoker ->
            registrar.handler(invoker.name) { data, callback ->
                invoker.invoke(
                    target = this,
                    invocation = BridgeInvocation(
                        host = host,
                        data = data,
                        callback = callback
                    )
                )
            }
        }
    }
}

private data class BridgeInvocation(
    val host: WebBridgeHost,
    val data: String?,
    val callback: WebBridgeCallback
)

private data class BridgeMethodInvoker(
    val name: String,
    val method: Method,
    val argumentProviders: List<(BridgeInvocation) -> Any?>,
    val replyMode: BridgeReplyMode
) {

    fun invoke(target: Any, invocation: BridgeInvocation) {
        try {
            method.isAccessible = true
            val result = method.invoke(
                target,
                *argumentProviders.map { provider -> provider(invocation) }.toTypedArray()
            )
            if (replyMode == BridgeReplyMode.AUTO) {
                invocation.replyWith(result)
            }
        } catch (throwable: Throwable) {
            val cause = if (throwable is InvocationTargetException) {
                throwable.targetException ?: throwable
            } else {
                throwable
            }
            Log.e("WebBridge", "invoke bridge handler failed, name=$name", cause)
            invocation.callback.failure(cause.message ?: "bridge invoke failed")
        }
    }
}

private enum class BridgeReplyMode {
    MANUAL,
    AUTO
}

private fun BridgeInvocation.replyWith(result: Any?) {
    when (result) {
        null, Unit -> callback.success()
        is WebBridgeResponse.Success -> callback.success(data = result.data, message = result.message)
        is WebBridgeResponse.Failure -> callback.failure(
            message = result.message,
            code = result.code,
            data = result.data
        )

        else -> callback.success(data = result)
    }
}

private object BridgeMethodRegistry {

    private val cache = ConcurrentHashMap<Class<*>, List<BridgeMethodInvoker>>()

    fun methodsFor(clazz: Class<*>): List<BridgeMethodInvoker> {
        return cache.getOrPut(clazz) {
            scan(clazz)
        }
    }

    private fun scan(clazz: Class<*>): List<BridgeMethodInvoker> {
        val methods = buildList {
            var current: Class<*>? = clazz
            while (current != null && current != AnnotatedWebBridgeModule::class.java && current != Any::class.java) {
                addAll(current.declaredMethods)
                current = current.superclass
            }
        }
        return methods
            .mapNotNull { method ->
                val annotation = method.getAnnotation(BridgeHandler::class.java) ?: return@mapNotNull null
                val replyMode = resolveReplyMode(method)
                BridgeMethodInvoker(
                    name = annotation.name,
                    method = method,
                    argumentProviders = method.parameterTypes.map { parameterType ->
                        parameterProvider(method, parameterType)
                    },
                    replyMode = replyMode
                )
            }
            .also { invokers ->
                val duplicateNames = invokers
                    .groupBy { it.name }
                    .filterValues { it.size > 1 }
                    .keys
                require(duplicateNames.isEmpty()) {
                    "Duplicate bridge handler names found in ${clazz.name}: ${duplicateNames.joinToString()}"
                }
            }
    }

    private fun resolveReplyMode(method: Method): BridgeReplyMode {
        val hasCallbackParameter = method.parameterTypes.any { parameterType ->
            WebBridgeCallback::class.java.isAssignableFrom(parameterType)
        }
        if (hasCallbackParameter) {
            return BridgeReplyMode.MANUAL
        }
        val extraAnnotations = method.annotations
            .map { it.annotationClass.java }
            .filter { it != BridgeHandler::class.java }
        require(extraAnnotations.isEmpty()) {
            val annotationNames = extraAnnotations.joinToString { it.simpleName }
            "Bridge handler ${method.declaringClass.name}#${method.name} uses $annotationNames; " +
                "add WebBridgeCallback parameter because automatic reply is unsafe with intercepted methods"
        }
        return BridgeReplyMode.AUTO
    }

    private fun parameterProvider(
        method: Method,
        parameterType: Class<*>
    ): (BridgeInvocation) -> Any? {
        return when {
            WebBridgeCallback::class.java.isAssignableFrom(parameterType) -> { invocation ->
                invocation.callback
            }

            WebBridgeHost::class.java.isAssignableFrom(parameterType) -> { invocation ->
                invocation.host
            }

            FragmentActivity::class.java.isAssignableFrom(parameterType) -> { invocation ->
                invocation.host.activity
            }

            Context::class.java.isAssignableFrom(parameterType) -> { invocation ->
                invocation.host.activity
            }

            Set::class.java.isAssignableFrom(parameterType) -> { invocation ->
                invocation.host.enabledBridgeGroups
            }

            parameterType == String::class.java -> { invocation ->
                invocation.data
            }

            else -> error(
                "Unsupported bridge parameter type ${parameterType.name} in ${method.declaringClass.name}#${method.name}"
            )
        }
    }
}
