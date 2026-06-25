package com.example.flowdownload

import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadRepository

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载库对外主接口，在基础仓库能力上补充恢复、清理和资源释放等生命周期操作。
 */
interface FlowDownloadClient : DownloadRepository {
    /**
     * 当前客户端的运行配置。
     */
    val config: FlowDownloadConfig

    /**
     * 按配置恢复中断任务与排队任务。
     *
     * 一般由库内部自动调用；宿主通常不需要手动触发。
     */
    suspend fun restore()

    /**
     * 删除指定任务记录，可选同时删除最终文件和分片文件。
     */
    suspend fun remove(
        id: DownloadId,
        deleteFinalFile: Boolean = false,
        deletePartialFile: Boolean = true,
    )

    /**
     * 批量删除任务记录，可选同时删除最终文件和分片文件。
     */
    suspend fun remove(
        ids: Collection<DownloadId>,
        deleteFinalFile: Boolean = false,
        deletePartialFile: Boolean = true,
    )

    /**
     * 按 `tag` 删除任务记录。
     */
    suspend fun removeByTag(
        tag: String,
        deleteFinalFiles: Boolean = false,
        deletePartialFiles: Boolean = true,
    )

    /**
     * 按 `group` 删除任务记录。
     */
    suspend fun removeByGroup(
        group: String,
        deleteFinalFiles: Boolean = false,
        deletePartialFiles: Boolean = true,
    )

    /**
     * 清理所有终态记录。
     */
    suspend fun clearTerminalRecords(
        deleteFinalFiles: Boolean = false,
        deletePartialFiles: Boolean = true,
    )

    /**
     * 按 `tag` 清理终态记录。
     */
    suspend fun clearTerminalRecordsByTag(
        tag: String,
        deleteFinalFiles: Boolean = false,
        deletePartialFiles: Boolean = true,
    )

    /**
     * 按 `group` 清理终态记录。
     */
    suspend fun clearTerminalRecordsByGroup(
        group: String,
        deleteFinalFiles: Boolean = false,
        deletePartialFiles: Boolean = true,
    )

    /**
     * 清理全部记录，包括活动态和终态。
     */
    suspend fun clearAllRecords(
        deleteFinalFiles: Boolean = false,
        deletePartialFiles: Boolean = true,
    )

    /**
     * 按 `tag` 清理全部记录。
     */
    suspend fun clearAllRecordsByTag(
        tag: String,
        deleteFinalFiles: Boolean = false,
        deletePartialFiles: Boolean = true,
    )

    /**
     * 按 `group` 清理全部记录。
     */
    suspend fun clearAllRecordsByGroup(
        group: String,
        deleteFinalFiles: Boolean = false,
        deletePartialFiles: Boolean = true,
    )

    /**
     * 关闭客户端并释放协程、数据库与通知观察等资源。
     */
    fun shutdown()
}
