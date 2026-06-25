package com.ghn.lib.upload

import android.content.Context
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

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
 * 描述: 上传服务实现，负责对外暴露统一的上传调用入口。
 */

@Single(binds = [UploadService::class])
class FlowUploadService(
    private val context: Context,
) : UploadService {
    private val appContext = context.applicationContext

    override fun observeAll(): Flow<List<UploadSnapshot>> {
        return FlowUpload.get(appContext).observeAll()
    }

    override fun observe(id: UploadId): Flow<UploadSnapshot?> {
        return FlowUpload.get(appContext).observe(id)
    }

    override suspend fun enqueue(request: UploadRequest): UploadId {
        return FlowUpload.get(appContext).enqueue(request)
    }

    override suspend fun get(id: UploadId): UploadSnapshot? {
        return FlowUpload.get(appContext).get(id)
    }

    override suspend fun cancel(id: UploadId) {
        FlowUpload.get(appContext).cancel(id)
    }

    override suspend fun retry(id: UploadId) {
        FlowUpload.get(appContext).retry(id)
    }
}
