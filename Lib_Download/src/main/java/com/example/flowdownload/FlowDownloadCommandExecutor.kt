package com.example.flowdownload

import com.example.flowdownload.download.model.DownloadId

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载命令执行抽象，供统一动作入口在不暴露仓库细节的前提下派发暂停、恢复、取消和重试命令。
 */
interface FlowDownloadCommandExecutor {
    suspend fun pause(downloadId: DownloadId)

    suspend fun resume(downloadId: DownloadId)

    suspend fun cancel(
        downloadId: DownloadId,
        deletePartialFile: Boolean,
    )

    suspend fun retry(downloadId: DownloadId)
}

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 基于 FlowDownloadClient 的默认命令执行器适配器，供宿主直接把下载客户端接入统一动作执行入口。
 */
class FlowDownloadClientCommandExecutor(
    private val client: FlowDownloadClient,
) : FlowDownloadCommandExecutor {
    override suspend fun pause(downloadId: DownloadId) {
        client.pause(downloadId)
    }

    override suspend fun resume(downloadId: DownloadId) {
        client.resume(downloadId)
    }

    override suspend fun cancel(
        downloadId: DownloadId,
        deletePartialFile: Boolean,
    ) {
        client.cancel(downloadId, deletePartialFile)
    }

    override suspend fun retry(downloadId: DownloadId) {
        client.retry(downloadId)
    }
}
