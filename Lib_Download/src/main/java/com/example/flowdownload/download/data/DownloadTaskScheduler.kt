package com.example.flowdownload.download.data

import com.example.flowdownload.download.model.DownloadId

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载调度抽象，负责排队、恢复、暂停和取消下载任务的运行时承载。
 */
interface DownloadTaskScheduler {
    suspend fun resumePending()

    suspend fun schedule(id: DownloadId)

    suspend fun pause(id: DownloadId)

    suspend fun cancel(id: DownloadId, deletePartialFile: Boolean)
}
