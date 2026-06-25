package com.example.flowdownload.download.data

import com.example.flowdownload.download.model.Clock
import com.example.flowdownload.download.model.DownloadFailure
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.PersistedDownload
import com.example.flowdownload.download.model.DownloadStatus
import com.example.flowdownload.download.network.DownloadStopController
import com.example.flowdownload.download.network.DownloadStopReason
import com.example.flowdownload.download.network.HttpDownloadEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 进程内下载调度器，实现基于优先级的并发分发、自动重试等待与暂停取消协作。
 */
class DownloadScheduler(
    private val store: DownloadStore,
    private val appScope: CoroutineScope,
    engine: HttpDownloadEngine,
    private val clock: Clock,
    private val maxConcurrentDownloads: Int = 2,
    private val retryBackoffMillis: Long = 30_000L,
) : DownloadTaskScheduler {
    private val schedulingMutex = Mutex()
    private val activeTasks = mutableMapOf<String, ActiveTask>()
    private val retryWaitTasks = mutableMapOf<String, Job>()
    private val runner = DownloadTaskRunner(
        store = store,
        engine = engine,
        clock = clock,
    )

    override suspend fun resumePending() {
        store.getQueued().forEach { schedule(it.id) }
    }

    override suspend fun schedule(id: DownloadId) {
        schedulingMutex.withLock {
            val record = store.get(id) ?: return
            when (val status = record.status) {
                is DownloadStatus.Queued -> {
                    if (activeTasks.containsKey(id.value)) return
                    dispatchEligibleTasksLocked()
                }

                is DownloadStatus.RetryWaiting -> {
                    ensureRetryWaitTaskLocked(
                        id = id,
                        nextAttemptAtEpochMs = status.nextAttemptAtEpochMs,
                    )
                }

                else -> return
            }
        }
    }

    override suspend fun pause(id: DownloadId) {
        stop(id, DownloadStopReason.Pause)
    }

    override suspend fun cancel(id: DownloadId, deletePartialFile: Boolean) {
        stop(
            id = id,
            reason = DownloadStopReason.Cancel(deletePartialFile = deletePartialFile),
        )
    }

    private suspend fun stop(id: DownloadId, reason: DownloadStopReason) {
        schedulingMutex.withLock {
            retryWaitTasks.remove(id.value)?.cancel()
            val activeTask = activeTasks[id.value] ?: return
            activeTask.controller.reason = reason
            activeTask.job.cancel()
        }
    }

    private suspend fun dispatchEligibleTasksLocked() {
        while (activeTasks.size < maxConcurrentDownloads) {
            val nextRecord = store.getQueued()
                .firstOrNull { record ->
                    record.status is DownloadStatus.Queued &&
                        !activeTasks.containsKey(record.id.value) &&
                        !retryWaitTasks.containsKey(record.id.value)
                } ?: break

            startActiveTaskLocked(nextRecord.id)
        }
    }

    private fun startActiveTaskLocked(id: DownloadId) {
        val controller = MutableStopController()
        val job = appScope.launch {
            try {
                executeActiveTask(id = id, stopController = controller)
            } finally {
                onActiveTaskFinished(id)
            }
        }
        activeTasks[id.value] = ActiveTask(
            job = job,
            controller = controller,
        )
    }

    private suspend fun executeActiveTask(
        id: DownloadId,
        stopController: DownloadStopController,
    ) {
        val result = try {
            runner.run(id = id, stopController = stopController)
        } catch (_: CancellationException) {
            return
        }

        if (result !is DownloadTaskRunResult.Failed) {
            return
        }

        val latest = store.get(id) ?: return
        if (!shouldAutoRetry(latest, result.failure)) {
            return
        }

        val now = clock.now()
        val nextAttemptAt = DownloadRetryBackoffCalculator.nextAttemptAtEpochMs(
            now = now,
            retryCount = latest.retryCount,
            baseBackoffMillis = retryBackoffMillis,
        )
        store.incrementRetryCount(id = id, now = now)
        store.markRetryWaiting(
            id = id,
            failure = result.failure,
            nextAttemptAtEpochMs = nextAttemptAt,
            now = now,
        )

        schedulingMutex.withLock {
            ensureRetryWaitTaskLocked(id = id, nextAttemptAtEpochMs = nextAttemptAt)
        }
    }

    private suspend fun onActiveTaskFinished(id: DownloadId) {
        schedulingMutex.withLock {
            activeTasks.remove(id.value)
            dispatchEligibleTasksLocked()
        }
    }

    private fun ensureRetryWaitTaskLocked(
        id: DownloadId,
        nextAttemptAtEpochMs: Long,
    ) {
        if (retryWaitTasks.containsKey(id.value)) {
            return
        }

        val job = appScope.launch {
            try {
                val delayMillis = DownloadRetryBackoffCalculator.remainingDelayMillis(
                    now = clock.now(),
                    nextAttemptAtEpochMs = nextAttemptAtEpochMs,
                )
                if (delayMillis > 0L) {
                    delay(delayMillis)
                }
                onRetryWindowReached(
                    id = id,
                    expectedNextAttemptAtEpochMs = nextAttemptAtEpochMs,
                )
            } finally {
                schedulingMutex.withLock {
                    retryWaitTasks.remove(id.value)
                }
            }
        }
        retryWaitTasks[id.value] = job
    }

    private suspend fun onRetryWindowReached(
        id: DownloadId,
        expectedNextAttemptAtEpochMs: Long,
    ) {
        schedulingMutex.withLock {
            retryWaitTasks.remove(id.value)
            val latest = store.get(id)
            val status = latest?.status as? DownloadStatus.RetryWaiting
            if (status != null && status.nextAttemptAtEpochMs == expectedNextAttemptAtEpochMs) {
                store.markQueued(
                    id = id,
                    now = clock.now(),
                    resetFailure = false,
                )
            }
            dispatchEligibleTasksLocked()
        }
    }

    private fun shouldAutoRetry(
        record: PersistedDownload,
        failure: DownloadFailure,
    ): Boolean {
        return failure.retryable && record.retryCount < record.maxRetries
    }

    private data class ActiveTask(
        val job: Job,
        val controller: MutableStopController,
    )
}

/**
 * 可变停止控制器，用于调度层向下载引擎传递暂停和取消信号。
 */
class MutableStopController : DownloadStopController {
    @Volatile
    override var reason: DownloadStopReason? = null
}
