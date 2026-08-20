package com.kotlinmvvm.core.ui.viewmodel

/**
 * @author 浩楠
 * @date 2026/7/21
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: ViewModel 任务调度事件，为日志、性能统计和崩溃定位提供稳定的观察节点
 */
enum class ViewModelTaskEvent(val diagnosticCode: String) {
    /** 任务已被接受并开始执行。 */
    STARTED("started"),

    /** 任务上游正常完成。 */
    COMPLETED("completed"),

    /** 任务因 ViewModel 清理或最新策略替换而取消。 */
    CANCELLED("cancelled"),

    /** 任务发生未被业务结果消费的意外异常。 */
    FAILED("failed"),

    /** 唯一策略发现同名任务已存在，本次调用未启动。 */
    SKIPPED("skipped"),

    /** 最新策略已用新任务替换同名旧任务。 */
    REPLACED("replaced")
}
