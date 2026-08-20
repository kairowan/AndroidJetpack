package com.kotlinmvvm.core.ui.viewmodel

/**
 * @author 浩楠
 * @date 2026/7/20
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: ViewModel 异步任务的公共意外异常，保留任务标识与原始原因供日志和崩溃平台定位
 */
class ViewModelTaskException(
    /** 触发异常的稳定任务标识，应与 BaseViewModel 启动任务时使用的标识一致。 */
    val taskKey: String,
    /** 未被业务结果模型处理的原始异常；仅供诊断，不应直接作为用户提示。 */
    val originalException: Exception
) : RuntimeException(
    "$taskKey | ${originalException::class.java.name} | ${originalException.message.orEmpty()}",
    originalException
)
