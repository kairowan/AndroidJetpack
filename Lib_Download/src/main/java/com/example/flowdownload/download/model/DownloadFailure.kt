package com.example.flowdownload.download.model

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载失败分类，用于区分网络、HTTP、校验和存储等不同失败场景。
 */
enum class DownloadFailureCategory {
    NETWORK_UNAVAILABLE,
    TIMEOUT,
    HTTP_4XX,
    HTTP_5XX,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    RANGE_NOT_SATISFIABLE,
    STORAGE_UNAVAILABLE,
    DISK_FULL,
    CHECKSUM_MISMATCH,
    FILE_CONFLICT,
    CANCELLED,
    PAUSED,
    UNKNOWN,
}

/**
 * 下载失败信息载体，包含失败分类、用户可读消息和是否允许重试。
 */
data class DownloadFailure(
    val category: DownloadFailureCategory,
    val message: String,
    val retryable: Boolean,
)
