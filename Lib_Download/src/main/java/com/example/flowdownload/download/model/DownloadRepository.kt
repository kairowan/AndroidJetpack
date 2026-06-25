package com.example.flowdownload.download.model

import kotlinx.coroutines.flow.Flow

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载仓库基础契约，定义命令式控制能力和基于 Flow 的状态、事件、摘要观察能力。
 */
interface DownloadRepository {
    /**
     * 观察所有任务的一次性事件流。
     *
     * 适合驱动 Toast、埋点、自定义通知和页面转场等非持久 UI。
     */
    fun observeEvents(): Flow<DownloadEvent>

    /**
     * 观察指定任务的一次性事件流。
     */
    fun observeEvents(id: DownloadId): Flow<DownloadEvent>

    /**
     * 观察指定任务的持续快照。
     */
    fun observe(id: DownloadId): Flow<DownloadSnapshot?>

    /**
     * 观察全部任务快照。
     */
    fun observeAll(): Flow<List<DownloadSnapshot>>

    /**
     * 按 URL 观察任务列表。
     */
    fun observeByUrl(url: String): Flow<List<DownloadSnapshot>>

    /**
     * 按目标地址观察任务列表。
     */
    fun observeByDestination(destination: String): Flow<List<DownloadSnapshot>>

    /**
     * 按业务标签观察任务列表。
     */
    fun observeByTag(tag: String): Flow<List<DownloadSnapshot>>

    /**
     * 按业务分组观察任务列表。
     */
    fun observeByGroup(group: String): Flow<List<DownloadSnapshot>>

    /**
     * 观察所有活动态任务。
     */
    fun observeActive(): Flow<List<DownloadSnapshot>>

    /**
     * 观察所有终态任务。
     */
    fun observeTerminal(): Flow<List<DownloadSnapshot>>

    /**
     * 观察全量摘要信息。
     */
    fun observeSummary(): Flow<DownloadSummary>

    /**
     * 按标签观察摘要信息。
     */
    fun observeSummaryByTag(tag: String): Flow<DownloadSummary>

    /**
     * 按分组观察摘要信息。
     */
    fun observeSummaryByGroup(group: String): Flow<DownloadSummary>

    /**
     * 读取指定任务的当前快照。
     */
    suspend fun get(id: DownloadId): DownloadSnapshot?

    /**
     * 读取全部任务快照。
     */
    suspend fun getAll(): List<DownloadSnapshot>

    /**
     * 按 URL 读取任务列表。
     */
    suspend fun getByUrl(url: String): List<DownloadSnapshot>

    /**
     * 按目标地址读取任务列表。
     */
    suspend fun getByDestination(destination: String): List<DownloadSnapshot>

    /**
     * 按标签读取任务列表。
     */
    suspend fun getByTag(tag: String): List<DownloadSnapshot>

    /**
     * 按分组读取任务列表。
     */
    suspend fun getByGroup(group: String): List<DownloadSnapshot>

    /**
     * 按 URL 查找最新一条记录。
     */
    suspend fun findByUrl(url: String): DownloadSnapshot?

    /**
     * 按目标地址查找最新一条记录。
     */
    suspend fun findByDestination(destination: String): DownloadSnapshot?

    /**
     * 读取所有活动态任务。
     */
    suspend fun getActive(): List<DownloadSnapshot>

    /**
     * 读取所有终态任务。
     */
    suspend fun getTerminal(): List<DownloadSnapshot>

    /**
     * 读取全量摘要信息。
     */
    suspend fun getSummary(): DownloadSummary

    /**
     * 按标签读取摘要信息。
     */
    suspend fun getSummaryByTag(tag: String): DownloadSummary

    /**
     * 按分组读取摘要信息。
     */
    suspend fun getSummaryByGroup(group: String): DownloadSummary

    /**
     * 入队单个任务，并返回更详细的冲突处理结果。
     */
    suspend fun enqueueDetailed(request: DownloadRequest): DownloadEnqueueResult

    /**
     * 批量入队任务，并返回每个请求对应的详细结果。
     */
    suspend fun enqueueDetailed(requests: Collection<DownloadRequest>): List<DownloadEnqueueResult>

    /**
     * 入队单个任务，并直接返回最终生效的下载 ID。
     */
    suspend fun enqueue(request: DownloadRequest): DownloadId

    /**
     * 批量入队任务，并返回最终生效的下载 ID 列表。
     */
    suspend fun enqueue(requests: Collection<DownloadRequest>): List<DownloadId>

    /**
     * 暂停指定任务。
     */
    suspend fun pause(id: DownloadId)

    /**
     * 暂停全部活动态任务。
     */
    suspend fun pauseAll()

    /**
     * 按标签暂停任务。
     */
    suspend fun pauseByTag(tag: String)

    /**
     * 按分组暂停任务。
     */
    suspend fun pauseByGroup(group: String)

    /**
     * 继续指定已暂停任务。
     */
    suspend fun resume(id: DownloadId)

    /**
     * 继续全部已暂停任务。
     */
    suspend fun resumeAll()

    /**
     * 按标签继续任务。
     */
    suspend fun resumeByTag(tag: String)

    /**
     * 按分组继续任务。
     */
    suspend fun resumeByGroup(group: String)

    /**
     * 取消指定任务。
     *
     * `deletePartialFile` 用于控制是否同时删除 `.part` 分片文件。
     */
    suspend fun cancel(id: DownloadId, deletePartialFile: Boolean = false)

    /**
     * 取消全部活动态任务。
     */
    suspend fun cancelAll(deletePartialFiles: Boolean = false)

    /**
     * 按标签取消任务。
     */
    suspend fun cancelByTag(tag: String, deletePartialFiles: Boolean = false)

    /**
     * 按分组取消任务。
     */
    suspend fun cancelByGroup(group: String, deletePartialFiles: Boolean = false)

    /**
     * 重试指定失败任务。
     */
    suspend fun retry(id: DownloadId)

    /**
     * 重试全部允许自动重试的失败任务。
     */
    suspend fun retryAllFailed()

    /**
     * 按标签重试失败任务。
     */
    suspend fun retryByTag(tag: String)

    /**
     * 按分组重试失败任务。
     */
    suspend fun retryByGroup(group: String)

    /**
     * 更新任务优先级，并返回更新后的快照。
     */
    suspend fun updatePriority(id: DownloadId, priority: Int): DownloadSnapshot?
}
