package com.kotlinmvvm.core.network.interceptor

import android.content.res.Resources
import com.kotlinmvvm.core.network.R

/**
 * @author 浩楠
 * @date 2026/7/20 11:30
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 网络日志格式使用的资源化文案快照，使拦截器不持有 Context 且便于独立测试
 */
data class NetworkLogLabels(
    val requestTitle: String,
    val responseTitle: String,
    val failureTitle: String,
    val requestId: String,
    val method: String,
    val url: String,
    val status: String,
    val duration: String,
    val requestHeaders: String,
    val requestBody: String,
    val responseHeaders: String,
    val responseBody: String,
    val exception: String,
    val empty: String,
    val redacted: String,
    val bodyDisabled: String,
    val binaryBodyOmitted: String,
    val unstructuredBodyOmitted: String,
    val oneShotBodyOmitted: String,
    val bodyReadFailed: String,
    val millisecondUnit: String
) {
    companion object {
        /** 从资源中一次性读取日志文案，后续网络请求不会持有或访问 Context。 */
        fun from(resources: Resources) = NetworkLogLabels(
            requestTitle = resources.getString(R.string.core_network_log_request_title),
            responseTitle = resources.getString(R.string.core_network_log_response_title),
            failureTitle = resources.getString(R.string.core_network_log_failure_title),
            requestId = resources.getString(R.string.core_network_log_request_id),
            method = resources.getString(R.string.core_network_log_method),
            url = resources.getString(R.string.core_network_log_url),
            status = resources.getString(R.string.core_network_log_status),
            duration = resources.getString(R.string.core_network_log_duration),
            requestHeaders = resources.getString(R.string.core_network_log_request_headers),
            requestBody = resources.getString(R.string.core_network_log_request_body),
            responseHeaders = resources.getString(R.string.core_network_log_response_headers),
            responseBody = resources.getString(R.string.core_network_log_response_body),
            exception = resources.getString(R.string.core_network_log_exception),
            empty = resources.getString(R.string.core_network_log_empty),
            redacted = resources.getString(R.string.core_network_log_redacted),
            bodyDisabled = resources.getString(R.string.core_network_log_body_disabled),
            binaryBodyOmitted = resources.getString(R.string.core_network_log_binary_body_omitted),
            unstructuredBodyOmitted = resources.getString(
                R.string.core_network_log_unstructured_body_omitted
            ),
            oneShotBodyOmitted = resources.getString(R.string.core_network_log_one_shot_body_omitted),
            bodyReadFailed = resources.getString(R.string.core_network_log_body_read_failed),
            millisecondUnit = resources.getString(R.string.core_network_log_millisecond_unit)
        )
    }
}
