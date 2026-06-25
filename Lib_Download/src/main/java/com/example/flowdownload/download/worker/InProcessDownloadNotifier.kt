package com.example.flowdownload.download.worker

import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.model.isTerminal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 进程内下载通知桥接器，负责在 IN_PROCESS 模式下监听快照变化并驱动系统通知更新、终态提示和移除清理。
 */
internal class InProcessDownloadNotifier(
    private val snapshots: Flow<List<DownloadSnapshot>>,
    private val dispatcher: Dispatcher,
    private val showTerminalNotifications: Boolean,
) {
    private var observeJob: Job? = null
    private val visibleActiveDownloadIds = linkedSetOf<DownloadId>()

    fun start(scope: CoroutineScope) {
        if (observeJob != null) {
            return
        }

        dispatcher.ensureChannel()
        observeJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            var previousSnapshotsById: Map<String, DownloadSnapshot>? = null
            snapshots.collect { currentSnapshots ->
                val currentSnapshotsById = currentSnapshots.associateBy { it.id.value }
                val commands = previousSnapshotsById?.let { previous ->
                    InProcessDownloadNotificationPlanner.planDiff(
                        previousSnapshotsById = previous,
                        currentSnapshots = currentSnapshots,
                        showTerminalNotifications = showTerminalNotifications,
                    )
                } ?: InProcessDownloadNotificationPlanner.planInitial(currentSnapshots)

                applyCommands(commands)
                previousSnapshotsById = currentSnapshotsById
            }
        }
    }

    fun stop() {
        observeJob?.cancel()
        observeJob = null
        val ids = visibleActiveDownloadIds.toList()
        visibleActiveDownloadIds.clear()
        ids.forEach(dispatcher::cancel)
    }

    private fun applyCommands(commands: List<InProcessDownloadNotificationCommand>) {
        commands.forEach { command ->
            when (command) {
                is InProcessDownloadNotificationCommand.ShowActive -> {
                    dispatcher.showActive(command.snapshot)
                    visibleActiveDownloadIds += command.snapshot.id
                }

                is InProcessDownloadNotificationCommand.ShowTerminal -> {
                    dispatcher.showTerminal(command.snapshot)
                    visibleActiveDownloadIds -= command.snapshot.id
                }

                is InProcessDownloadNotificationCommand.Cancel -> {
                    dispatcher.cancel(command.downloadId)
                    visibleActiveDownloadIds -= command.downloadId
                }
            }
        }
    }

    internal interface Dispatcher {
        fun ensureChannel()

        fun showActive(snapshot: DownloadSnapshot)

        fun showTerminal(snapshot: DownloadSnapshot)

        fun cancel(downloadId: DownloadId)
    }
}

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 进程内通知命令规划器，负责把前后快照差异转换成展示、终态通知和取消通知等可执行命令。
 */
internal object InProcessDownloadNotificationPlanner {
    fun planInitial(
        currentSnapshots: List<DownloadSnapshot>,
    ): List<InProcessDownloadNotificationCommand> {
        return currentSnapshots
            .filter(DownloadSnapshot::isActiveLike)
            .map(InProcessDownloadNotificationCommand::ShowActive)
    }

    fun planDiff(
        previousSnapshotsById: Map<String, DownloadSnapshot>,
        currentSnapshots: List<DownloadSnapshot>,
        showTerminalNotifications: Boolean,
    ): List<InProcessDownloadNotificationCommand> {
        val commands = mutableListOf<InProcessDownloadNotificationCommand>()
        val currentSnapshotsById = currentSnapshots.associateBy { it.id.value }

        currentSnapshots.forEach { snapshot ->
            val previous = previousSnapshotsById[snapshot.id.value]
            when {
                snapshot.isTerminal -> {
                    if (previous == null || previous != snapshot) {
                        commands += if (showTerminalNotifications) {
                            InProcessDownloadNotificationCommand.ShowTerminal(snapshot)
                        } else {
                            InProcessDownloadNotificationCommand.Cancel(snapshot.id)
                        }
                    }
                }

                previous == null || previous != snapshot -> {
                    commands += InProcessDownloadNotificationCommand.ShowActive(snapshot)
                }
            }
        }

        previousSnapshotsById.keys
            .asSequence()
            .filterNot(currentSnapshotsById::containsKey)
            .map(::DownloadId)
            .forEach { downloadId ->
                commands += InProcessDownloadNotificationCommand.Cancel(downloadId)
            }

        return commands
    }
}

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 进程内通知执行命令模型，用于解耦快照差异规划与 Android 通知实际下发动作。
 */
internal sealed interface InProcessDownloadNotificationCommand {
    data class ShowActive(
        val snapshot: DownloadSnapshot,
    ) : InProcessDownloadNotificationCommand

    data class ShowTerminal(
        val snapshot: DownloadSnapshot,
    ) : InProcessDownloadNotificationCommand

    data class Cancel(
        val downloadId: DownloadId,
    ) : InProcessDownloadNotificationCommand
}
