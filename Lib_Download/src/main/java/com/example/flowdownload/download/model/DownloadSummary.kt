package com.example.flowdownload.download.model

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载队列汇总模型，供宿主快速读取当前队列规模、状态分布与活跃吞吐信息。
 */
data class DownloadSummary(
    val totalCount: Int,
    val activeCount: Int,
    val terminalCount: Int,
    val queuedCount: Int,
    val retryWaitingCount: Int,
    val runningCount: Int,
    val pausedCount: Int,
    val successCount: Int,
    val failedCount: Int,
    val cancelledCount: Int,
    val activeBytesDownloaded: Long,
    val activeKnownTotalBytes: Long?,
    val runningBytesPerSecond: Long?,
) {
    companion object {
        fun fromSnapshots(snapshots: List<DownloadSnapshot>): DownloadSummary {
            var queuedCount = 0
            var retryWaitingCount = 0
            var runningCount = 0
            var pausedCount = 0
            var successCount = 0
            var failedCount = 0
            var cancelledCount = 0
            var activeBytesDownloaded = 0L
            var activeKnownTotalBytes = 0L
            var hasKnownActiveTotal = false
            var runningBytesPerSecond = 0L
            var hasRunningSpeed = false

            snapshots.forEach { snapshot ->
                when (val status = snapshot.status) {
                    is DownloadStatus.Queued -> queuedCount += 1
                    is DownloadStatus.RetryWaiting -> retryWaitingCount += 1
                    is DownloadStatus.Starting -> Unit
                    is DownloadStatus.Running -> {
                        runningCount += 1
                        status.bytesPerSecond?.let { speed ->
                            hasRunningSpeed = true
                            runningBytesPerSecond += speed
                        }
                    }

                    is DownloadStatus.Paused -> pausedCount += 1
                    is DownloadStatus.Success -> successCount += 1
                    is DownloadStatus.Failed -> failedCount += 1
                    is DownloadStatus.Cancelled -> cancelledCount += 1
                }

                if (snapshot.isActiveLike) {
                    activeBytesDownloaded += snapshot.bytesDownloaded
                    snapshot.totalBytes?.let { totalBytes ->
                        hasKnownActiveTotal = true
                        activeKnownTotalBytes += totalBytes
                    }
                }
            }

            val activeCount = snapshots.count(DownloadSnapshot::isActiveLike)
            return DownloadSummary(
                totalCount = snapshots.size,
                activeCount = activeCount,
                terminalCount = snapshots.size - activeCount,
                queuedCount = queuedCount,
                retryWaitingCount = retryWaitingCount,
                runningCount = runningCount,
                pausedCount = pausedCount,
                successCount = successCount,
                failedCount = failedCount,
                cancelledCount = cancelledCount,
                activeBytesDownloaded = activeBytesDownloaded,
                activeKnownTotalBytes = activeKnownTotalBytes.takeIf { hasKnownActiveTotal },
                runningBytesPerSecond = runningBytesPerSecond.takeIf { hasRunningSpeed },
            )
        }
    }
}
