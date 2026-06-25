package com.ghn.lib.download

import android.content.Context
import android.os.Build
import android.os.Environment
import android.webkit.URLUtil
import com.example.flowdownload.FlowDownload
import com.example.flowdownload.FlowDownloadMediaStore
import com.example.flowdownload.download.model.DownloadConflictPolicy
import com.example.flowdownload.download.model.DownloadEnqueueResult
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadRequest
import com.example.flowdownload.download.model.DownloadSnapshot
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import java.io.File

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
 * 描述: 下载服务实现，负责对外暴露统一的下载调用入口。
 */

@Single(binds = [DownloadService::class])
class FlowDownloadService(
    private val context: Context,
) : DownloadService {
    private val appContext: Context = context.applicationContext

    override fun observe(id: DownloadId): Flow<DownloadSnapshot?> {
        return FlowDownload.get(appContext).observe(id)
    }

    override fun observeAll(): Flow<List<DownloadSnapshot>> {
        return FlowDownload.get(appContext).observeAll()
    }

    override suspend fun enqueue(request: DownloadTaskRequest): DownloadEnqueueResult {
        return FlowDownload.get(appContext).enqueueDetailed(request.toDownloadRequest(appContext))
    }

    override suspend fun get(id: DownloadId): DownloadSnapshot? {
        return FlowDownload.get(appContext).get(id)
    }

    override suspend fun pause(id: DownloadId) {
        FlowDownload.get(appContext).pause(id)
    }

    override suspend fun resume(id: DownloadId) {
        FlowDownload.get(appContext).resume(id)
    }

    override suspend fun cancel(
        id: DownloadId,
        deletePartialFile: Boolean,
    ) {
        FlowDownload.get(appContext).cancel(id, deletePartialFile)
    }
}

private fun DownloadTaskRequest.toDownloadRequest(context: Context): DownloadRequest {
    val resolvedDisplayName = displayName
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?: URLUtil.guessFileName(url, null, mimeType)
    val conflictPolicy = if (overwriteExisting) {
        DownloadConflictPolicy.REPLACE_EXISTING
    } else {
        DownloadConflictPolicy.REUSE_EXISTING
    }
    if (preferPublicDownloads && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return FlowDownloadMediaStore.downloads(
            context = context,
            url = url,
            displayName = resolvedDisplayName,
            relativePath = mediaStoreRelativePath(subDirectory),
            mimeType = mimeType,
            headers = headers,
            tag = tag,
            group = group,
            extras = extras,
            conflictPolicy = conflictPolicy,
            overwriteExisting = overwriteExisting,
            maxRetries = maxRetries.coerceAtLeast(0),
            priority = priority,
        )
    }
    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        ?: File(context.filesDir, "downloads")
    val targetDir = normalizedSubDirectory(subDirectory)
        ?.let { File(baseDir, it) }
        ?: baseDir
    val destination = File(targetDir, resolvedDisplayName)
    return DownloadRequest(
        url = url,
        destination = destination.absolutePath,
        displayName = resolvedDisplayName,
        headers = headers,
        tag = tag,
        group = group,
        extras = extras,
        conflictPolicy = conflictPolicy,
        overwriteExisting = overwriteExisting,
        maxRetries = maxRetries.coerceAtLeast(0),
        priority = priority,
    )
}

private fun mediaStoreRelativePath(subDirectory: String?): String {
    val normalized = normalizedSubDirectory(subDirectory)
    return if (normalized.isNullOrBlank()) {
        "Download"
    } else {
        "Download/$normalized"
    }
}

private fun normalizedSubDirectory(value: String?): String? {
    return value
        ?.replace('\\', '/')
        ?.split('/')
        ?.map(String::trim)
        ?.filter(String::isNotBlank)
        ?.joinToString("/")
        ?.takeIf(String::isNotBlank)
}
