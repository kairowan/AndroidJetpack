package com.example.flowdownload.download.data

import java.io.File

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载路径工具，负责统一规范最终文件路径、临时文件路径和默认展示名称。
 */
object DownloadPathResolver {
    fun isContentUri(destination: String): Boolean =
        destination.trim().startsWith(prefix = "content://", ignoreCase = true)

    fun tempPathFor(
        destination: String,
        explicitTempFilePath: String? = null,
    ): String {
        return explicitTempFilePath?.let { File(it).canonicalFile.absolutePath }
            ?: "$destination.part"
    }

    fun canonicalize(path: String): String {
        return if (isContentUri(path)) {
            path.trim()
        } else {
            File(path).canonicalFile.absolutePath
        }
    }

    fun displayNameFor(destination: String, explicitName: String?): String =
        explicitName?.takeIf(String::isNotBlank)
            ?: if (isContentUri(destination)) {
                destination.substringAfterLast('/').ifBlank { destination }
            } else {
                File(destination).name
            }
}
