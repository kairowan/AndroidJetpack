package com.example.flowdownload.download.data

import com.example.flowdownload.FlowDownloadClient
import com.example.flowdownload.FlowDownloadConfig
import com.example.flowdownload.download.model.Clock
import com.example.flowdownload.download.model.DownloadConstraints
import com.example.flowdownload.download.model.DownloadConflictPolicy
import com.example.flowdownload.download.model.DownloadEnqueueResult
import com.example.flowdownload.download.model.DownloadEvent
import com.example.flowdownload.download.model.DownloadFailureCategory
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadRequest
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.model.DownloadStatus
import com.example.flowdownload.download.model.DownloadSummary
import com.example.flowdownload.download.model.isTerminal
import com.example.flowdownload.download.storage.DownloadDestinationAccess
import com.example.flowdownload.download.storage.LocalFileDownloadDestinationAccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载库主仓库实现，对外暴露命令 API，并协调恢复、清理和调度行为。
 */
class DefaultDownloadRepository(
    private val store: DownloadStore,
    private val scheduler: DownloadTaskScheduler,
    private val appScope: CoroutineScope,
    private val clock: Clock,
    override val config: FlowDownloadConfig,
    private val destinationAccess: DownloadDestinationAccess = LocalFileDownloadDestinationAccess,
    private val onStart: () -> Unit = {},
    private val onRestored: () -> Unit = {},
    private val onShutdown: () -> Unit = {},
) : FlowDownloadClient {
    init {
        appScope.launch {
            onStart()
            restore()
            onRestored()
        }
    }

    override fun observeEvents(): Flow<DownloadEvent> = flow {
        var previousSnapshotsById: Map<String, DownloadSnapshot>? = null
        store.observeAll().collect { snapshots ->
            val currentSnapshotsById = snapshots.associateBy { it.id.value }
            previousSnapshotsById?.let { previous ->
                buildRepositoryEvents(
                    previousSnapshotsById = previous,
                    currentSnapshots = snapshots,
                    currentSnapshotsById = currentSnapshotsById,
                ).forEach { event ->
                    emit(event)
                }
            }
            previousSnapshotsById = currentSnapshotsById
        }
    }

    override fun observeEvents(id: DownloadId): Flow<DownloadEvent> =
        observeEvents().filter { event -> event.downloadId == id }

    override fun observe(id: DownloadId): Flow<DownloadSnapshot?> = store.observe(id)

    override fun observeAll(): Flow<List<DownloadSnapshot>> = store.observeAll()

    override fun observeByUrl(url: String): Flow<List<DownloadSnapshot>> {
        val normalizedUrl = requireSelectorValue(name = "url", value = url)
        return store.observeByUrl(normalizedUrl)
    }

    override fun observeByDestination(destination: String): Flow<List<DownloadSnapshot>> {
        val normalizedDestination = canonicalizeSelectorDestination(destination)
        return store.observeByDestination(normalizedDestination)
    }

    override fun observeByTag(tag: String): Flow<List<DownloadSnapshot>> {
        val normalizedTag = requireSelectorValue(name = "tag", value = tag)
        return store.observeByTag(normalizedTag)
    }

    override fun observeByGroup(group: String): Flow<List<DownloadSnapshot>> {
        val normalizedGroup = requireSelectorValue(name = "group", value = group)
        return store.observeByGroup(normalizedGroup)
    }

    override fun observeActive(): Flow<List<DownloadSnapshot>> =
        observeAll().map { snapshots ->
            snapshots.filter(DownloadSnapshot::isActiveLike)
        }

    override fun observeTerminal(): Flow<List<DownloadSnapshot>> =
        observeAll().map { snapshots ->
            snapshots.filter(DownloadSnapshot::isTerminal)
        }

    override fun observeSummary(): Flow<DownloadSummary> =
        observeAll().map(DownloadSummary::fromSnapshots)

    override fun observeSummaryByTag(tag: String): Flow<DownloadSummary> =
        observeByTag(tag).map(DownloadSummary::fromSnapshots)

    override fun observeSummaryByGroup(group: String): Flow<DownloadSummary> =
        observeByGroup(group).map(DownloadSummary::fromSnapshots)

    override suspend fun get(id: DownloadId): DownloadSnapshot? =
        store.get(id)?.let(DownloadMapper::persistedToSnapshot)

    override suspend fun getAll(): List<DownloadSnapshot> =
        store.getAll().map(DownloadMapper::persistedToSnapshot)

    override suspend fun getByUrl(url: String): List<DownloadSnapshot> {
        val normalizedUrl = requireSelectorValue(name = "url", value = url)
        return store.getByUrl(normalizedUrl).map(DownloadMapper::persistedToSnapshot)
    }

    override suspend fun getByDestination(destination: String): List<DownloadSnapshot> {
        val normalizedDestination = canonicalizeSelectorDestination(destination)
        return store.getByDestination(normalizedDestination).map(DownloadMapper::persistedToSnapshot)
    }

    override suspend fun getByTag(tag: String): List<DownloadSnapshot> {
        val normalizedTag = requireSelectorValue(name = "tag", value = tag)
        return store.getByTag(normalizedTag).map(DownloadMapper::persistedToSnapshot)
    }

    override suspend fun getByGroup(group: String): List<DownloadSnapshot> {
        val normalizedGroup = requireSelectorValue(name = "group", value = group)
        return store.getByGroup(normalizedGroup).map(DownloadMapper::persistedToSnapshot)
    }

    override suspend fun findByUrl(url: String): DownloadSnapshot? =
        getByUrl(url).maxByOrNull(DownloadSnapshot::updatedAtEpochMs)

    override suspend fun findByDestination(destination: String): DownloadSnapshot? =
        getByDestination(destination).maxByOrNull(DownloadSnapshot::updatedAtEpochMs)

    override suspend fun getActive(): List<DownloadSnapshot> =
        store.getActive().map(DownloadMapper::persistedToSnapshot)

    override suspend fun getTerminal(): List<DownloadSnapshot> =
        store.getTerminal().map(DownloadMapper::persistedToSnapshot)

    override suspend fun getSummary(): DownloadSummary =
        DownloadSummary.fromSnapshots(getAll())

    override suspend fun getSummaryByTag(tag: String): DownloadSummary =
        DownloadSummary.fromSnapshots(getByTag(tag))

    override suspend fun getSummaryByGroup(group: String): DownloadSummary =
        DownloadSummary.fromSnapshots(getByGroup(group))

    override suspend fun enqueueDetailed(request: DownloadRequest): DownloadEnqueueResult {
        return enqueueDetailed(listOf(request)).single()
    }

    override suspend fun enqueueDetailed(
        requests: Collection<DownloadRequest>,
    ): List<DownloadEnqueueResult> {
        if (requests.isEmpty()) {
            return emptyList()
        }

        val preparedRequests = prepareRequests(requests)
        preparedRequests.filterIsInstance<PreparedEnqueue.Create>()
            .map(PreparedEnqueue.Create::normalizedRequest)
            .forEach { request ->
                store.insertQueued(request, clock.now())
            }
        preparedRequests.filterIsInstance<PreparedEnqueue.Replace>()
            .forEach { replacement ->
                remove(
                    id = replacement.existingSnapshot.id,
                    deleteFinalFile = false,
                    deletePartialFile = true,
                )
                store.insertQueued(replacement.normalizedRequest, clock.now())
            }
        preparedRequests.forEach { prepared ->
            when (prepared) {
                is PreparedEnqueue.Create -> scheduler.schedule(prepared.normalizedRequest.id)
                is PreparedEnqueue.Replace -> scheduler.schedule(prepared.normalizedRequest.id)
                is PreparedEnqueue.Reuse -> Unit
            }
        }
        return preparedRequests.map { prepared ->
            when (prepared) {
                is PreparedEnqueue.Create -> DownloadEnqueueResult.Created(
                    request = prepared.normalizedRequest,
                    downloadId = prepared.normalizedRequest.id,
                )

                is PreparedEnqueue.Reuse -> DownloadEnqueueResult.ReusedExisting(
                    request = prepared.normalizedRequest,
                    existingSnapshot = prepared.existingSnapshot,
                )

                is PreparedEnqueue.Replace -> DownloadEnqueueResult.ReplacedExisting(
                    request = prepared.normalizedRequest,
                    replacedSnapshot = prepared.existingSnapshot,
                    downloadId = prepared.normalizedRequest.id,
                )
            }
        }
    }

    override suspend fun enqueue(request: DownloadRequest): DownloadId {
        return enqueueDetailed(request).downloadId
    }

    override suspend fun enqueue(requests: Collection<DownloadRequest>): List<DownloadId> {
        return enqueueDetailed(requests).map(DownloadEnqueueResult::downloadId)
    }

    override suspend fun pause(id: DownloadId) {
        val record = store.get(id) ?: return
        when (record.status) {
            is DownloadStatus.Queued -> {
                store.markPaused(id = id, now = clock.now())
                scheduler.pause(id)
            }

            is DownloadStatus.RetryWaiting -> {
                store.markPaused(
                    id = id,
                    now = clock.now(),
                    bytesDownloaded = record.bytesDownloaded,
                    totalBytes = record.totalBytes,
                    eTag = record.eTag,
                    lastModified = record.lastModified,
                )
                scheduler.pause(id)
            }

            is DownloadStatus.Starting,
            is DownloadStatus.Running,
            -> {
                store.markPauseRequested(id = id, now = clock.now())
                scheduler.pause(id)
            }

            else -> return
        }
    }

    override suspend fun pauseAll() {
        store.getActive().forEach { record ->
            pause(record.id)
        }
    }

    override suspend fun pauseByTag(tag: String) {
        performOnSnapshots(
            snapshots = getByTag(tag),
            predicate = { it.canPause },
        ) { snapshot ->
            pause(snapshot.id)
        }
    }

    override suspend fun pauseByGroup(group: String) {
        performOnSnapshots(
            snapshots = getByGroup(group),
            predicate = { it.canPause },
        ) { snapshot ->
            pause(snapshot.id)
        }
    }

    override suspend fun resume(id: DownloadId) {
        val record = store.get(id) ?: return
        if (record.status !is DownloadStatus.Paused) return
        store.markQueued(id = id, now = clock.now(), resetFailure = true)
        scheduler.schedule(id)
    }

    override suspend fun resumeAll() {
        store.getActive()
            .filter { it.status is DownloadStatus.Paused }
            .forEach { record ->
                resume(record.id)
            }
    }

    override suspend fun resumeByTag(tag: String) {
        performOnSnapshots(
            snapshots = getByTag(tag),
            predicate = { it.canResume },
        ) { snapshot ->
            resume(snapshot.id)
        }
    }

    override suspend fun resumeByGroup(group: String) {
        performOnSnapshots(
            snapshots = getByGroup(group),
            predicate = { it.canResume },
        ) { snapshot ->
            resume(snapshot.id)
        }
    }

    override suspend fun cancel(id: DownloadId, deletePartialFile: Boolean) {
        val record = store.get(id) ?: return
        when (record.status) {
            is DownloadStatus.RetryWaiting -> {
                scheduler.cancel(id, deletePartialFile)
                if (deletePartialFile) {
                    File(record.tempDestination).delete()
                }
                store.markCancelled(id = id, now = clock.now())
            }

            is DownloadStatus.Starting,
            is DownloadStatus.Running,
            -> {
                store.markCancelRequested(
                    id = id,
                    deletePartialFile = deletePartialFile,
                    now = clock.now(),
                )
                scheduler.cancel(id, deletePartialFile)
            }

            else -> {
                scheduler.cancel(id, deletePartialFile)
                if (deletePartialFile) {
                    File(record.tempDestination).delete()
                }
                store.markCancelled(id = id, now = clock.now())
            }
        }
    }

    override suspend fun cancelAll(deletePartialFiles: Boolean) {
        store.getActive().forEach { record ->
            cancel(
                id = record.id,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun cancelByTag(tag: String, deletePartialFiles: Boolean) {
        performOnSnapshots(
            snapshots = getByTag(tag),
            predicate = { it.canCancel },
        ) { snapshot ->
            cancel(
                id = snapshot.id,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun cancelByGroup(group: String, deletePartialFiles: Boolean) {
        performOnSnapshots(
            snapshots = getByGroup(group),
            predicate = { it.canCancel },
        ) { snapshot ->
            cancel(
                id = snapshot.id,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun retry(id: DownloadId) {
        val record = store.get(id) ?: return
        val failed = record.status as? DownloadStatus.Failed
            ?: throw IllegalStateException("Only failed downloads can be retried")
        if (!failed.failure.retryable) {
            throw IllegalStateException("Failure is not retryable: ${failed.failure.category}")
        }
        if (record.retryCount >= record.maxRetries) {
            throw IllegalStateException("Retry limit reached for ${id.value}")
        }

        val tempFile = File(record.tempDestination)
        if (failed.failure.category == DownloadFailureCategory.CHECKSUM_MISMATCH && tempFile.exists()) {
            tempFile.delete()
        }

        store.incrementRetryCount(id, clock.now())
        store.markQueued(id = id, now = clock.now(), resetFailure = true)
        scheduler.schedule(id)
    }

    override suspend fun retryAllFailed() {
        store.getTerminal()
            .filter { snapshot ->
                val failed = snapshot.status as? DownloadStatus.Failed
                failed != null && failed.failure.retryable && snapshot.retryCount < snapshot.maxRetries
            }
            .forEach { record ->
                retry(record.id)
            }
    }

    override suspend fun retryByTag(tag: String) {
        performOnSnapshots(
            snapshots = getByTag(tag),
            predicate = { it.canRetry },
        ) { snapshot ->
            retry(snapshot.id)
        }
    }

    override suspend fun retryByGroup(group: String) {
        performOnSnapshots(
            snapshots = getByGroup(group),
            predicate = { it.canRetry },
        ) { snapshot ->
            retry(snapshot.id)
        }
    }

    override suspend fun updatePriority(id: DownloadId, priority: Int): DownloadSnapshot? {
        store.updatePriority(
            id = id,
            priority = priority,
            now = clock.now(),
        )
        val updated = get(id)
        val status = updated?.status
        if (status is DownloadStatus.Queued || status is DownloadStatus.RetryWaiting) {
            scheduler.schedule(id)
        }
        return updated
    }

    override suspend fun restore() {
        if (config.autoPauseInterruptedDownloads) {
            store.recoverInterrupted(clock.now())
        }
        if (config.autoRecoverQueuedDownloads) {
            scheduler.resumePending()
        }
    }

    override suspend fun remove(
        id: DownloadId,
        deleteFinalFile: Boolean,
        deletePartialFile: Boolean,
    ) {
        val record = store.get(id) ?: return
        if (!record.status.isTerminal) {
            cancel(id = id, deletePartialFile = deletePartialFile)
        }

        if (deleteFinalFile) {
            destinationAccess.delete(record.destination)
        }
        if (deletePartialFile) {
            File(record.tempDestination).delete()
        }
        store.delete(id)
    }

    override suspend fun remove(
        ids: Collection<DownloadId>,
        deleteFinalFile: Boolean,
        deletePartialFile: Boolean,
    ) {
        ids.forEach { id ->
            remove(
                id = id,
                deleteFinalFile = deleteFinalFile,
                deletePartialFile = deletePartialFile,
            )
        }
    }

    override suspend fun removeByTag(
        tag: String,
        deleteFinalFiles: Boolean,
        deletePartialFiles: Boolean,
    ) {
        performOnSnapshots(getByTag(tag)) { snapshot ->
            remove(
                id = snapshot.id,
                deleteFinalFile = deleteFinalFiles,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun removeByGroup(
        group: String,
        deleteFinalFiles: Boolean,
        deletePartialFiles: Boolean,
    ) {
        performOnSnapshots(getByGroup(group)) { snapshot ->
            remove(
                id = snapshot.id,
                deleteFinalFile = deleteFinalFiles,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun clearTerminalRecords(
        deleteFinalFiles: Boolean,
        deletePartialFiles: Boolean,
    ) {
        store.getTerminal().forEach { record ->
            remove(
                id = record.id,
                deleteFinalFile = deleteFinalFiles,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun clearTerminalRecordsByTag(
        tag: String,
        deleteFinalFiles: Boolean,
        deletePartialFiles: Boolean,
    ) {
        performOnSnapshots(
            snapshots = getByTag(tag),
            predicate = { it.isTerminal },
        ) { snapshot ->
            remove(
                id = snapshot.id,
                deleteFinalFile = deleteFinalFiles,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun clearTerminalRecordsByGroup(
        group: String,
        deleteFinalFiles: Boolean,
        deletePartialFiles: Boolean,
    ) {
        performOnSnapshots(
            snapshots = getByGroup(group),
            predicate = { it.isTerminal },
        ) { snapshot ->
            remove(
                id = snapshot.id,
                deleteFinalFile = deleteFinalFiles,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun clearAllRecords(
        deleteFinalFiles: Boolean,
        deletePartialFiles: Boolean,
    ) {
        store.getAll().forEach { record ->
            remove(
                id = record.id,
                deleteFinalFile = deleteFinalFiles,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun clearAllRecordsByTag(
        tag: String,
        deleteFinalFiles: Boolean,
        deletePartialFiles: Boolean,
    ) {
        performOnSnapshots(getByTag(tag)) { snapshot ->
            remove(
                id = snapshot.id,
                deleteFinalFile = deleteFinalFiles,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override suspend fun clearAllRecordsByGroup(
        group: String,
        deleteFinalFiles: Boolean,
        deletePartialFiles: Boolean,
    ) {
        performOnSnapshots(getByGroup(group)) { snapshot ->
            remove(
                id = snapshot.id,
                deleteFinalFile = deleteFinalFiles,
                deletePartialFile = deletePartialFiles,
            )
        }
    }

    override fun shutdown() {
        onShutdown()
    }

    private suspend fun prepareRequests(
        requests: Collection<DownloadRequest>,
    ): List<PreparedEnqueue> {
        val preparedRequests = mutableListOf<PreparedEnqueue>()
        val seenDestinations = linkedSetOf<String>()

        requests.forEach { request ->
            require(request.url.isNotBlank()) {
                "Download url must not be blank"
            }
            require(request.destination.isNotBlank()) {
                "Download destination must not be blank"
            }
            require(request.maxRetries >= 0) {
                "maxRetries must be greater than or equal to 0"
            }
            require(request.extras.keys.none(String::isBlank)) {
                "Download extras keys must not be blank"
            }

            validateConstraints(request.constraints)

            val destination = destinationAccess.canonicalize(request.destination)
            require(seenDestinations.add(destination)) {
                "Duplicate destination in batch enqueue: $destination"
            }

            val normalizedRequest = request.copy(
                destination = destination,
                displayName = destinationAccess.resolveDisplayName(destination, request.displayName),
                tag = request.tag?.trim()?.takeIf(String::isNotBlank),
                group = request.group?.trim()?.takeIf(String::isNotBlank),
                tempFilePath = destinationAccess.resolveTempFilePath(
                    destination = destination,
                    explicitTempFilePath = request.tempFilePath,
                ),
            )
            val existing = store.findActiveByDestination(destination)
            when {
                existing != null && normalizedRequest.conflictPolicy == DownloadConflictPolicy.REJECT -> {
                    throw IllegalStateException("A download already owns destination $destination")
                }

                existing != null && normalizedRequest.conflictPolicy == DownloadConflictPolicy.REUSE_EXISTING -> {
                    preparedRequests += PreparedEnqueue.Reuse(
                        normalizedRequest = normalizedRequest,
                        existingSnapshot = DownloadMapper.persistedToSnapshot(existing),
                    )
                    return@forEach
                }

                existing != null && normalizedRequest.conflictPolicy == DownloadConflictPolicy.REPLACE_EXISTING -> {
                    preparedRequests += PreparedEnqueue.Replace(
                        normalizedRequest = normalizedRequest,
                        existingSnapshot = DownloadMapper.persistedToSnapshot(existing),
                    )
                    return@forEach
                }
            }

            if (destinationAccess.exists(destination) && !normalizedRequest.overwriteExisting) {
                throw IllegalStateException("Destination already exists: $destination")
            }

            preparedRequests += PreparedEnqueue.Create(
                normalizedRequest = normalizedRequest,
            )
        }

        return preparedRequests
    }

    private fun validateConstraints(constraints: DownloadConstraints) {
        if (config.runtimeMode == com.example.flowdownload.DownloadRuntimeMode.IN_PROCESS &&
            constraints.requiresSystemScheduler
        ) {
            throw IllegalStateException(
                "Download constraints require WORK_MANAGER runtime mode: $constraints",
            )
        }
    }

    private fun requireSelectorValue(name: String, value: String): String {
        return value.trim().takeIf(String::isNotBlank)
            ?: throw IllegalArgumentException("Download $name must not be blank")
    }

    private suspend fun performOnSnapshots(
        snapshots: List<DownloadSnapshot>,
        predicate: (DownloadSnapshot) -> Boolean = { true },
        action: suspend (DownloadSnapshot) -> Unit,
    ) {
        snapshots
            .filter(predicate)
            .forEach { snapshot ->
                action(snapshot)
            }
    }

    private fun buildRepositoryEvents(
        previousSnapshotsById: Map<String, DownloadSnapshot>,
        currentSnapshots: List<DownloadSnapshot>,
        currentSnapshotsById: Map<String, DownloadSnapshot>,
    ): List<DownloadEvent> {
        val events = mutableListOf<DownloadEvent>()
        currentSnapshots.forEach { currentSnapshot ->
            val previousSnapshot = previousSnapshotsById[currentSnapshot.id.value]
            appendPriorityChangedEventIfNeeded(events, previousSnapshot, currentSnapshot)
            appendStatusEventIfNeeded(events, previousSnapshot, currentSnapshot)
            appendProgressEventIfNeeded(events, previousSnapshot, currentSnapshot)
        }

        previousSnapshotsById.values
            .filterNot { snapshot -> currentSnapshotsById.containsKey(snapshot.id.value) }
            .forEach { removedSnapshot ->
                events += DownloadEvent.Removed(snapshot = removedSnapshot)
            }
        return events
    }

    private fun appendPriorityChangedEventIfNeeded(
        events: MutableList<DownloadEvent>,
        previousSnapshot: DownloadSnapshot?,
        currentSnapshot: DownloadSnapshot,
    ) {
        if (previousSnapshot == null || previousSnapshot.priority == currentSnapshot.priority) {
            return
        }
        events +=
            DownloadEvent.PriorityChanged(
                snapshot = currentSnapshot,
                previousPriority = previousSnapshot.priority,
                currentPriority = currentSnapshot.priority,
            )
    }

    private fun appendStatusEventIfNeeded(
        events: MutableList<DownloadEvent>,
        previousSnapshot: DownloadSnapshot?,
        currentSnapshot: DownloadSnapshot,
    ) {
        val previousStatusKey = previousSnapshot?.status?.eventKey()
        val currentStatusKey = currentSnapshot.status.eventKey()
        if (previousStatusKey == currentStatusKey) {
            return
        }

        when (val status = currentSnapshot.status) {
            DownloadStatus.Queued -> {
                events += DownloadEvent.Enqueued(snapshot = currentSnapshot)
            }

            is DownloadStatus.RetryWaiting -> {
                events +=
                    DownloadEvent.RetryWaiting(
                        snapshot = currentSnapshot,
                        failure = status.failure,
                        nextAttemptAtEpochMs = status.nextAttemptAtEpochMs,
                    )
            }

            DownloadStatus.Starting -> {
                events += DownloadEvent.Started(snapshot = currentSnapshot)
            }

            is DownloadStatus.Running -> {
                if (previousSnapshot?.status !is DownloadStatus.Starting) {
                    events += DownloadEvent.Started(snapshot = currentSnapshot)
                }
            }

            DownloadStatus.Paused -> {
                events += DownloadEvent.Paused(snapshot = currentSnapshot)
            }

            DownloadStatus.Cancelled -> {
                events += DownloadEvent.Cancelled(snapshot = currentSnapshot)
            }

            DownloadStatus.Success -> {
                events += DownloadEvent.Success(snapshot = currentSnapshot)
            }

            is DownloadStatus.Failed -> {
                events +=
                    DownloadEvent.Failed(
                        snapshot = currentSnapshot,
                        failure = status.failure,
                    )
            }
        }
    }

    private fun appendProgressEventIfNeeded(
        events: MutableList<DownloadEvent>,
        previousSnapshot: DownloadSnapshot?,
        currentSnapshot: DownloadSnapshot,
    ) {
        if (currentSnapshot.status !is DownloadStatus.Running || previousSnapshot == null) {
            return
        }
        if (currentSnapshot.bytesDownloaded <= previousSnapshot.bytesDownloaded) {
            return
        }
        events +=
            DownloadEvent.Progress(
                snapshot = currentSnapshot,
                previousBytesDownloaded = previousSnapshot.bytesDownloaded,
                deltaBytes = currentSnapshot.bytesDownloaded - previousSnapshot.bytesDownloaded,
            )
    }

    private fun canonicalizeSelectorDestination(destination: String): String {
        val normalizedDestination = requireSelectorValue(name = "destination", value = destination)
        return destinationAccess.canonicalize(normalizedDestination)
    }

    private fun DownloadStatus.eventKey(): String {
        return when (this) {
            DownloadStatus.Queued -> "queued"
            is DownloadStatus.RetryWaiting -> "retry_waiting"
            DownloadStatus.Starting -> "starting"
            is DownloadStatus.Running -> "running"
            DownloadStatus.Paused -> "paused"
            DownloadStatus.Cancelled -> "cancelled"
            DownloadStatus.Success -> "success"
            is DownloadStatus.Failed -> "failed"
        }
    }

    private sealed interface PreparedEnqueue {
        val normalizedRequest: DownloadRequest

        data class Create(
            override val normalizedRequest: DownloadRequest,
        ) : PreparedEnqueue

        data class Reuse(
            override val normalizedRequest: DownloadRequest,
            val existingSnapshot: DownloadSnapshot,
        ) : PreparedEnqueue

        data class Replace(
            override val normalizedRequest: DownloadRequest,
            val existingSnapshot: DownloadSnapshot,
        ) : PreparedEnqueue
    }
}
