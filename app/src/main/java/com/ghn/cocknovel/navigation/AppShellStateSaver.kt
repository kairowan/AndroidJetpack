package com.ghn.cocknovel.navigation

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.kotlinmvvm.core.navigation.AppShellState

/**
 * @author 浩楠
 *
 * @date 2026-3-11
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: App 壳层共享状态的 Compose 保存适配器
 */
internal object AppShellStateSaver {
    val Saver: Saver<AppShellState, Any> = listSaver(
        save = { listOf(it.isShortsFullscreen, it.shortsDeactivateSignal) },
        restore = { restored ->
            AppShellState(
                isShortsFullscreen = restored[0] as Boolean,
                shortsDeactivateSignal = restored[1] as Int
            )
        }
    )
}
