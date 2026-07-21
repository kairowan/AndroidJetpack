package com.kotlinmvvm.core.ui.viewmodel

/**
 * @author 浩楠
 * @date 2026/7/20 17:51
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: ViewModel 任务诊断观察契约，允许上层接入日志或监控且不介入业务状态
 */
fun interface ViewModelTaskObserver {
    /**
     * 接收 [taskKey] 对应的 [event]。
     *
     * [exception] 仅在任务失败时提供；实现必须线程安全，且不应抛出异常影响业务任务。
     */
    fun onTaskEvent(
        taskKey: String,
        event: ViewModelTaskEvent,
        exception: ViewModelTaskException?
    )

    companion object {
        val None = ViewModelTaskObserver { _, _, _ -> }
    }
}
