package com.example.flowdownload.download.model

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载事件模型，向宿主暴露一次性的入队、进度、成功、失败和移除事件，便于自定义 UI、通知和埋点消费。
 */
sealed interface DownloadEvent {
    val downloadId: DownloadId
    val occurredAtEpochMs: Long

    data class Enqueued(
        val snapshot: DownloadSnapshot,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class Started(
        val snapshot: DownloadSnapshot,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class Progress(
        val snapshot: DownloadSnapshot,
        val previousBytesDownloaded: Long,
        val deltaBytes: Long,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class RetryWaiting(
        val snapshot: DownloadSnapshot,
        val failure: DownloadFailure,
        val nextAttemptAtEpochMs: Long,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class Paused(
        val snapshot: DownloadSnapshot,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class Cancelled(
        val snapshot: DownloadSnapshot,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class Success(
        val snapshot: DownloadSnapshot,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class Failed(
        val snapshot: DownloadSnapshot,
        val failure: DownloadFailure,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class PriorityChanged(
        val snapshot: DownloadSnapshot,
        val previousPriority: Int,
        val currentPriority: Int,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }

    data class Removed(
        val snapshot: DownloadSnapshot,
    ) : DownloadEvent {
        override val downloadId: DownloadId
            get() = snapshot.id

        override val occurredAtEpochMs: Long
            get() = snapshot.updatedAtEpochMs
    }
}
