package com.ghn.lib.base.startup

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import java.util.concurrent.Executors

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
 * 描述: 启动任务定义与调度器实现，负责按阶段编排应用初始化流程
 */

/**
 * 启动阶段定义
 *
 * 将启动任务拆成首屏前、首帧后主线程和首帧后后台三类，
 * 方便我们在保证依赖时序的前提下尽量缩短冷启动主线程占用。
 */
enum class StartupPhase {
    BLOCKING_MAIN,
    AFTER_FIRST_FRAME_MAIN,
    AFTER_FIRST_FRAME_BACKGROUND,
}

/**
 * 启动任务动作抽象。
 */
fun interface StartupAction {
    fun run(application: Application)
}

/**
 * 启动任务描述模型。
 */
data class StartupTask(
    val name: String,
    val phase: StartupPhase,
    val action: StartupAction,
)

/**
 * 启动任务调度器。
 */
object StartupTaskRunner {
    private const val TAG = "AppStartup"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun run(
        application: Application,
        tasks: List<StartupTask>,
    ) {
        val blockingTasks = tasks.filter { it.phase == StartupPhase.BLOCKING_MAIN }
        val firstFrameMainTasks = tasks.filter { it.phase == StartupPhase.AFTER_FIRST_FRAME_MAIN }
        val firstFrameBackgroundTasks =
            tasks.filter { it.phase == StartupPhase.AFTER_FIRST_FRAME_BACKGROUND }

        // 首屏依赖仍然在冷启动阶段同步完成，避免页面创建后才发现核心能力未就绪。
        blockingTasks.forEach { runTask(application, it) }

        if (firstFrameMainTasks.isEmpty() && firstFrameBackgroundTasks.isEmpty()) {
            return
        }

        // 首帧完成后再补齐非关键初始化，避免继续挤占 Application 冷启动时间片。
        scheduleAfterFirstFrame {
            firstFrameMainTasks.forEach { runTask(application, it) }
            if (firstFrameBackgroundTasks.isEmpty()) {
                return@scheduleAfterFirstFrame
            }
            // 后台预热任务串行执行，避免首帧后瞬时拉起过多并发初始化。
            val executor = Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "startup-background")
            }
            executor.execute {
                try {
                    firstFrameBackgroundTasks.forEach { runTask(application, it) }
                } finally {
                    executor.shutdown()
                }
            }
        }
    }

    private fun scheduleAfterFirstFrame(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            postAfterFirstFrame(action)
            return
        }
        mainHandler.post {
            postAfterFirstFrame(action)
        }
    }

    private fun postAfterFirstFrame(action: () -> Unit) {
        Choreographer.getInstance().postFrameCallback {
            mainHandler.post(action)
        }
    }

    private fun runTask(
        application: Application,
        task: StartupTask,
    ) {
        try {
            task.action.run(application)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Startup task failed: ${task.name}", throwable)
        }
    }
}
