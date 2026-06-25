package com.example.flowdownload.download.model

import java.util.UUID

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载任务唯一标识，避免对外暴露原始字符串并统一任务 ID 生成规则。
 */
@JvmInline
value class DownloadId(val value: String) {
    companion object {
        fun newId(): DownloadId = DownloadId(UUID.randomUUID().toString())
    }
}
