package com.ghn.lib.upload

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
 * 描述: 上传仓储接口，约定上传任务管理与状态访问能力。
 */

interface UploadRepository {
    fun observeAll(): Flow<List<UploadSnapshot>>

    fun observe(id: UploadId): Flow<UploadSnapshot?>

    fun observeEvents(): Flow<UploadEvent>

    suspend fun enqueue(request: UploadRequest): UploadId

    suspend fun get(id: UploadId): UploadSnapshot?

    suspend fun cancel(id: UploadId)

    suspend fun retry(id: UploadId)
}
