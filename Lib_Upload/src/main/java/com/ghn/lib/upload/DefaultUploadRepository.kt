package com.ghn.lib.upload

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.Call
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

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
 * 描述: 上传仓储默认实现，负责调度上传任务并维护状态流。
 */

internal class DefaultUploadRepository(
    context: Context,
    okHttpClient: OkHttpClient,
    private val appScope: CoroutineScope,
    override val config: FlowUploadConfig,
) : FlowUploadClient {
    private val appContext = context.applicationContext
    private val engine = OkHttpUploadEngine(appContext, okHttpClient, config)
    // 通过信号量控制并发上传数，避免同时创建过多网络请求压垮前台进程。
    private val semaphore = Semaphore(config.maxConcurrentUploads.coerceAtLeast(1))
    private val state = MutableStateFlow<Map<String, UploadRecord>>(emptyMap())
    private val events = MutableSharedFlow<UploadEvent>(extraBufferCapacity = 64)
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val activeCalls = ConcurrentHashMap<String, Call>()

    override fun observeAll(): Flow<List<UploadSnapshot>> {
        return state.map { records ->
            records.values
                .map(UploadRecord::snapshot)
                .sortedByDescending(UploadSnapshot::updatedAtEpochMs)
        }
    }

    override fun observe(id: UploadId): Flow<UploadSnapshot?> {
        return state.map { records -> records[id.value]?.snapshot }
    }

    override fun observeEvents(): Flow<UploadEvent> = events

    override suspend fun enqueue(request: UploadRequest): UploadId {
        val normalized = request.normalized(appContext)
        val now = System.currentTimeMillis()
        val snapshot = UploadSnapshot(
            id = normalized.id,
            url = normalized.url,
            fileUri = normalized.fileUri,
            fileName = normalized.fileName.orEmpty(),
            fieldName = normalized.fieldName,
            mimeType = normalized.mimeType,
            method = normalized.method,
            tag = normalized.tag,
            group = normalized.group,
            extras = normalized.extras,
            status = UploadStatus.QUEUED,
            bytesUploaded = 0L,
            totalBytes = UploadSourceResolver.contentLength(appContext, normalized.fileUri),
            progressPercent = 0,
            responseCode = null,
            responseBody = null,
            errorMessage = null,
            retryCount = 0,
            maxRetries = normalized.maxRetries,
            priority = normalized.priority,
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
        )
        upsertRecord(
            UploadRecord(
                request = normalized,
                snapshot = snapshot
            )
        )
        events.tryEmit(UploadEvent.Enqueued(normalized.id, snapshot))
        schedule(normalized.id)
        return normalized.id
    }

    override suspend fun get(id: UploadId): UploadSnapshot? {
        return state.value[id.value]?.snapshot
    }

    override suspend fun cancel(id: UploadId) {
        val record = state.value[id.value] ?: return
        activeCalls.remove(id.value)?.cancel()
        activeJobs.remove(id.value)?.cancel()
        val cancelled = record.snapshot.copy(
            status = UploadStatus.CANCELLED,
            errorMessage = "upload cancelled",
            updatedAtEpochMs = System.currentTimeMillis()
        )
        upsertSnapshot(id, cancelled)
        events.tryEmit(UploadEvent.Cancelled(id, cancelled))
    }

    override suspend fun retry(id: UploadId) {
        val record = state.value[id.value] ?: return
        if (record.snapshot.status != UploadStatus.FAILED) {
            return
        }
        val resetSnapshot = record.snapshot.copy(
            status = UploadStatus.QUEUED,
            bytesUploaded = 0L,
            progressPercent = 0,
            responseCode = null,
            responseBody = null,
            errorMessage = null,
            retryCount = 0,
            updatedAtEpochMs = System.currentTimeMillis()
        )
        upsertSnapshot(id, resetSnapshot)
        schedule(id)
    }

    override fun shutdown() {
        activeCalls.values.forEach(Call::cancel)
        activeJobs.values.forEach(Job::cancel)
        activeCalls.clear()
        activeJobs.clear()
        appScope.cancel()
    }

    private fun schedule(id: UploadId) {
        if (activeJobs.containsKey(id.value)) {
            return
        }
        val job = appScope.launch {
            semaphore.withPermit {
                executeUpload(id)
            }
        }
        activeJobs[id.value] = job
        job.invokeOnCompletion {
            activeJobs.remove(id.value)
            activeCalls.remove(id.value)
        }
    }

    private suspend fun executeUpload(id: UploadId) {
        val record = state.value[id.value] ?: return
        var attempt = record.snapshot.retryCount
        while (true) {
            val latestRecord = state.value[id.value] ?: return
            if (latestRecord.snapshot.status == UploadStatus.CANCELLED) {
                return
            }
            val runningSnapshot = latestRecord.snapshot.copy(
                status = UploadStatus.RUNNING,
                updatedAtEpochMs = System.currentTimeMillis()
            )
            upsertSnapshot(id, runningSnapshot)
            events.tryEmit(UploadEvent.Started(id, runningSnapshot))
            val throttle = ProgressThrottle(config.progressThrottleMillis)
            try {
                val response = engine.execute(
                    request = latestRecord.request,
                    onProgress = progress@{ bytesUploaded, totalBytes ->
                        if (!throttle.shouldEmit(bytesUploaded, totalBytes)) {
                            return@progress
                        }
                        val progressPercent = totalBytes
                            ?.takeIf { it > 0L }
                            ?.let { bytesUploaded * 100 / it }
                            ?.toInt()
                            ?.coerceIn(0, 100)
                        val progressSnapshot = state.value[id.value]?.snapshot?.copy(
                            status = UploadStatus.RUNNING,
                            bytesUploaded = bytesUploaded,
                            totalBytes = totalBytes,
                            progressPercent = progressPercent,
                            updatedAtEpochMs = System.currentTimeMillis()
                        ) ?: return@progress
                        upsertSnapshot(id, progressSnapshot)
                        events.tryEmit(UploadEvent.Progress(id, progressSnapshot))
                    },
                    onCallCreated = { call ->
                        activeCalls[id.value] = call
                    }
                )
                val successSnapshot = state.value[id.value]?.snapshot?.copy(
                    status = UploadStatus.SUCCESS,
                    bytesUploaded = state.value[id.value]?.snapshot?.totalBytes
                        ?: state.value[id.value]?.snapshot?.bytesUploaded
                        ?: 0L,
                    progressPercent = 100,
                    responseCode = response.code,
                    responseBody = response.body,
                    errorMessage = null,
                    updatedAtEpochMs = System.currentTimeMillis()
                ) ?: return
                upsertSnapshot(id, successSnapshot)
                events.tryEmit(UploadEvent.Success(id, successSnapshot))
                return
            } catch (cancelled: CancellationException) {
                return
            } catch (throwable: Throwable) {
                if (state.value[id.value]?.snapshot?.status == UploadStatus.CANCELLED) {
                    return
                }
                // 可重试错误先进入等待态，再按退避时间回到队列，避免立即重放同一请求。
                if (throwable.shouldRetry() && attempt < latestRecord.request.maxRetries) {
                    attempt += 1
                    val retrySnapshot = latestRecord.snapshot.copy(
                        status = UploadStatus.RETRY_WAITING,
                        retryCount = attempt,
                        errorMessage = throwable.message,
                        updatedAtEpochMs = System.currentTimeMillis()
                    )
                    upsertSnapshot(id, retrySnapshot)
                    delay(config.retryBackoffMillis)
                    if (state.value[id.value]?.snapshot?.status == UploadStatus.CANCELLED) {
                        return
                    }
                    val queuedSnapshot = state.value[id.value]?.snapshot?.copy(
                        status = UploadStatus.QUEUED,
                        updatedAtEpochMs = System.currentTimeMillis()
                    ) ?: return
                    upsertSnapshot(id, queuedSnapshot)
                    continue
                }
                val failedSnapshot = latestRecord.snapshot.copy(
                    status = UploadStatus.FAILED,
                    responseCode = (throwable as? UploadHttpException)?.code,
                    responseBody = (throwable as? UploadHttpException)?.responseBody,
                    errorMessage = throwable.message ?: "upload failed",
                    retryCount = attempt,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
                upsertSnapshot(id, failedSnapshot)
                events.tryEmit(UploadEvent.Failed(id, failedSnapshot))
                return
            } finally {
                activeCalls.remove(id.value)
            }
        }
    }

    private fun upsertRecord(record: UploadRecord) {
        state.value = state.value.toMutableMap().apply {
            put(record.snapshot.id.value, record)
        }
    }

    private fun upsertSnapshot(id: UploadId, snapshot: UploadSnapshot) {
        val current = state.value[id.value] ?: return
        upsertRecord(current.copy(snapshot = snapshot))
    }
}

private data class UploadRecord(
    val request: UploadRequest,
    val snapshot: UploadSnapshot,
)

private class ProgressThrottle(
    private val throttleMillis: Long,
) {
    private var lastEmitAt: Long = 0L
    private var lastBytes: Long = -1L

    fun shouldEmit(
        bytesUploaded: Long,
        totalBytes: Long?,
    ): Boolean {
        val now = SystemClock.elapsedRealtime()
        val finished = totalBytes != null && totalBytes > 0L && bytesUploaded >= totalBytes
        if (finished || bytesUploaded == 0L) {
            lastEmitAt = now
            lastBytes = bytesUploaded
            return true
        }
        if (bytesUploaded == lastBytes) {
            return false
        }
        if (now - lastEmitAt < throttleMillis) {
            return false
        }
        lastEmitAt = now
        lastBytes = bytesUploaded
        return true
    }
}

private fun UploadRequest.normalized(context: Context): UploadRequest {
    val normalizedMethod = method.trim().uppercase()
    require(normalizedMethod in setOf("POST", "PUT", "PATCH")) {
        "Unsupported upload method: $method"
    }
    val normalizedFieldName = fieldName.trim().ifBlank { "file" }
    val normalizedFileName = UploadSourceResolver.resolveDisplayName(
        context = context,
        source = fileUri,
        fallback = fileName
    )
    val normalizedMimeType = UploadSourceResolver.resolveMimeType(
        context = context,
        source = fileUri,
        fallback = mimeType
    )
    return copy(
        fileName = normalizedFileName,
        mimeType = normalizedMimeType,
        method = normalizedMethod,
        fieldName = normalizedFieldName,
        maxRetries = maxRetries.coerceAtLeast(0)
    )
}

private fun Throwable.shouldRetry(): Boolean {
    return when (this) {
        is UploadHttpException -> code >= 500
        is IOException -> true
        else -> false
    }
}
