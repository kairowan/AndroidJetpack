package com.ghn.lib.download

import com.example.flowdownload.download.model.DownloadEnqueueResult
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadSnapshot
import kotlinx.coroutines.flow.Flow

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
 * 描述: 下载服务接口，负责定义宿主可调用的下载能力。
 */

interface DownloadService {
    fun observe(id: DownloadId): Flow<DownloadSnapshot?>

    fun observeAll(): Flow<List<DownloadSnapshot>>

    suspend fun enqueue(request: DownloadTaskRequest): DownloadEnqueueResult

    suspend fun get(id: DownloadId): DownloadSnapshot?

    suspend fun pause(id: DownloadId)

    suspend fun resume(id: DownloadId)

    suspend fun cancel(
        id: DownloadId,
        deletePartialFile: Boolean = false,
    )
}
