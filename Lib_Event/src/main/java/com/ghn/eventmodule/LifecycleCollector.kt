package com.ghn.eventmodule

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * @author 浩楠
 * @date 2025/6/16
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO collect 的辅助扩展函数
 */
fun <T> Flow<T>.collectIn(owner: LifecycleOwner, block: suspend (T) -> Unit) {
    owner.lifecycleScope.launch {
        collect { block(it) }
    }
}

fun <T> Flow<T>.collectIn(scope: CoroutineScope, block: suspend (T) -> Unit) {
    scope.launch {
        collect { block(it) }
    }
}


