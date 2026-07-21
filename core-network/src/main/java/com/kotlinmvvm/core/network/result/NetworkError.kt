package com.kotlinmvvm.core.network.result

/**
 * @author 浩楠
 * @date 2026/7/20 15:26
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 通用网络失败结果，携带已归类且不会泄漏 Retrofit 类型的 NetworkFailure
 */
data class NetworkError(val error: NetworkFailure) : NetworkResult<Nothing>
