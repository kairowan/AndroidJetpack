package com.example.flowdownload.download.storage

import java.io.File
import java.io.IOException

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载目标访问抽象，统一屏蔽文件路径与其他目标介质在规范化、冲突检测、删除和最终落盘上的差异。
 */
interface DownloadDestinationAccess {
    fun canonicalize(destination: String): String

    fun resolveTempFilePath(destination: String, explicitTempFilePath: String?): String

    fun resolveDisplayName(destination: String, explicitDisplayName: String?): String

    @Throws(IOException::class)
    fun prepareDestination(destination: String, tempFile: File)

    fun exists(destination: String): Boolean

    fun delete(destination: String): Boolean

    @Throws(IOException::class)
    fun finalizeDownload(tempFile: File, destination: String, overwriteExisting: Boolean)
}

/**
 * 本地文件目标实现，负责目录准备、冲突检查和临时文件到最终文件的原子落盘。
 */
internal object LocalFileDownloadDestinationAccess : DownloadDestinationAccess {
    override fun canonicalize(destination: String): String = File(destination).canonicalFile.absolutePath

    override fun resolveTempFilePath(
        destination: String,
        explicitTempFilePath: String?,
    ): String {
        return explicitTempFilePath?.let { File(it).canonicalFile.absolutePath }
            ?: "$destination.part"
    }

    override fun resolveDisplayName(
        destination: String,
        explicitDisplayName: String?,
    ): String {
        return explicitDisplayName?.takeIf(String::isNotBlank)
            ?: File(destination).name
    }

    override fun prepareDestination(destination: String, tempFile: File) {
        ensureParentDirectory(File(destination))
        ensureParentDirectory(tempFile)
    }

    override fun exists(destination: String): Boolean = File(destination).exists()

    override fun delete(destination: String): Boolean = File(destination).delete()

    override fun finalizeDownload(
        tempFile: File,
        destination: String,
        overwriteExisting: Boolean,
    ) {
        val destinationFile = File(destination)
        ensureParentDirectory(destinationFile)

        if (destinationFile.exists()) {
            if (!overwriteExisting) {
                throw IOException("Destination already exists: ${destinationFile.absolutePath}")
            }
            if (!destinationFile.delete()) {
                throw IOException("Failed to replace ${destinationFile.absolutePath}")
            }
        }

        if (!tempFile.renameTo(destinationFile)) {
            tempFile.inputStream().use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (!tempFile.delete()) {
                tempFile.deleteOnExit()
            }
        }
    }

    private fun ensureParentDirectory(file: File) {
        val parent = file.parentFile ?: return
        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("Failed to create directory ${parent.absolutePath}")
        }
    }
}
