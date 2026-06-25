package com.ghn.lib.download

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
 * 描述: 下载任务请求模型，描述下载任务所需的业务参数。
 */

data class DownloadTaskRequest(
    val url: String,
    val displayName: String? = null,
    val subDirectory: String? = null,
    val mimeType: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val tag: String? = null,
    val group: String? = null,
    val extras: Map<String, String> = emptyMap(),
    val overwriteExisting: Boolean = true,
    val preferPublicDownloads: Boolean = true,
    val maxRetries: Int = 2,
    val priority: Int = 0,
)
