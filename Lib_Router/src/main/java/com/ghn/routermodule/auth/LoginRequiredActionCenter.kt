package com.ghn.routermodule.auth

import android.util.Log
import java.util.UUID

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
 * 描述: LoginRequiredActionCenter 中心组件，负责统一管理相关状态或动作分发。
 */

object LoginRequiredActionCenter {

    private const val TAG = "LoginRequiredAction"

    private val lock = Any()
    private var awaitingLogin = false
    private var activeSessionId: String? = null
    private val pendingActions = LinkedHashMap<String, PendingAction>()

    fun enqueue(actionKey: String, action: () -> Unit): LoginEnqueueResult {
        synchronized(lock) {
            val shouldOpenLogin = !awaitingLogin
            val sessionId = activeSessionId ?: UUID.randomUUID().toString().also {
                activeSessionId = it
            }
            awaitingLogin = true
            pendingActions[actionKey] = PendingAction(
                key = actionKey,
                sessionId = sessionId,
                action = action
            )
            Log.d(
                TAG,
                "enqueue action=$actionKey session=$sessionId pendingCount=${pendingActions.size} shouldOpenLogin=$shouldOpenLogin"
            )
            return LoginEnqueueResult(
                sessionId = sessionId,
                shouldOpenLogin = shouldOpenLogin
            )
        }
    }

    fun onLoginSuccess(sessionId: String? = null) {
        val actions = synchronized(lock) {
            val currentSessionId = activeSessionId
            if (currentSessionId == null) {
                Log.d(TAG, "ignore login success because no active session")
                return
            }
            if (sessionId != null && sessionId != currentSessionId) {
                Log.d(TAG, "ignore login success for stale session=$sessionId current=$currentSessionId")
                return
            }
            val current = pendingActions.values
                .filter { it.sessionId == currentSessionId }
                .toList()
            pendingActions.clear()
            awaitingLogin = false
            activeSessionId = null
            current
        }
        if (actions.isEmpty()) {
            Log.d(TAG, "login success without pending action")
            return
        }
        Log.d(TAG, "resume pending actions count=${actions.size}")
        actions.forEach { action ->
            Log.d(TAG, "resume action=${action.key}")
            action.action.invoke()
        }
    }

    fun clearPendingAction(sessionId: String? = null) {
        synchronized(lock) {
            val currentSessionId = activeSessionId
            if (sessionId != null && currentSessionId != null && sessionId != currentSessionId) {
                Log.d(TAG, "ignore clear pending actions for stale session=$sessionId current=$currentSessionId")
                return
            }
            if (pendingActions.isNotEmpty() || awaitingLogin) {
                Log.d(
                    TAG,
                    "clear pending actions count=${pendingActions.size} session=${currentSessionId.orEmpty()}"
                )
            }
            pendingActions.clear()
            awaitingLogin = false
            activeSessionId = null
        }
    }

    fun hasPendingAction(sessionId: String? = null): Boolean {
        return synchronized(lock) {
            val currentSessionId = activeSessionId
            when {
                sessionId == null -> pendingActions.isNotEmpty()
                currentSessionId == null || currentSessionId != sessionId -> false
                else -> pendingActions.values.any { it.sessionId == sessionId }
            }
        }
    }

    data class LoginEnqueueResult(
        val sessionId: String,
        val shouldOpenLogin: Boolean
    )

    private data class PendingAction(
        val key: String,
        val sessionId: String,
        val action: () -> Unit
    )
}
