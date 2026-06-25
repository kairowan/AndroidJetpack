package com.example.flowdownload.download.model

import android.net.Uri

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载请求模型，定义 URL、目标路径、校验策略和队列优先级等输入参数。
 */
data class DownloadRequest(
    val id: DownloadId = DownloadId.newId(),
    val url: String,
    val destination: String,
    val displayName: String? = null,
    val tag: String? = null,
    val group: String? = null,
    val extras: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val constraints: DownloadConstraints = DownloadConstraints(),
    val expectedSha256: String? = null,
    val conflictPolicy: DownloadConflictPolicy = DownloadConflictPolicy.REJECT,
    val overwriteExisting: Boolean = false,
    val maxRetries: Int = 2,
    val priority: Int = 0,
    val tempFilePath: String? = null,
) {
    companion object {
        fun contentUri(
            url: String,
            destinationUri: Uri,
            displayName: String? = null,
            tag: String? = null,
            group: String? = null,
            extras: Map<String, String> = emptyMap(),
            headers: Map<String, String> = emptyMap(),
            constraints: DownloadConstraints = DownloadConstraints(),
            expectedSha256: String? = null,
            conflictPolicy: DownloadConflictPolicy = DownloadConflictPolicy.REJECT,
            overwriteExisting: Boolean = true,
            maxRetries: Int = 2,
            priority: Int = 0,
            tempFilePath: String? = null,
            id: DownloadId = DownloadId.newId(),
        ): DownloadRequest {
            return DownloadRequest(
                id = id,
                url = url,
                destination = destinationUri.toString(),
                displayName = displayName,
                tag = tag,
                group = group,
                extras = extras,
                headers = headers,
                constraints = constraints,
                expectedSha256 = expectedSha256,
                conflictPolicy = conflictPolicy,
                overwriteExisting = overwriteExisting,
                maxRetries = maxRetries,
                priority = priority,
                tempFilePath = tempFilePath,
            )
        }
    }
}
