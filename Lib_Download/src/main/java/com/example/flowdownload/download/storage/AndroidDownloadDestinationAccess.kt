package com.example.flowdownload.download.storage

import android.content.Context
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Android 平台下载目标实现，额外支持 MediaStore 和 SAF content Uri 目标写入。
 */
internal class AndroidDownloadDestinationAccess(
    private val context: Context,
) : DownloadDestinationAccess {
    private val contentResolver = context.contentResolver
    private val partialRoot by lazy {
        File(context.cacheDir, "flowdownload/partial")
    }

    override fun canonicalize(destination: String): String {
        val trimmed = destination.trim()
        return if (isContentDestination(trimmed)) {
            Uri.parse(trimmed).normalizeScheme().toString()
        } else {
            LocalFileDownloadDestinationAccess.canonicalize(trimmed)
        }
    }

    override fun resolveTempFilePath(
        destination: String,
        explicitTempFilePath: String?,
    ): String {
        explicitTempFilePath?.takeIf(String::isNotBlank)?.let {
            return File(it).canonicalFile.absolutePath
        }
        if (!isContentDestination(destination)) {
            return LocalFileDownloadDestinationAccess.resolveTempFilePath(
                destination = destination,
                explicitTempFilePath = null,
            )
        }

        val suffix = extensionSuffix(resolveDisplayName(destination, explicitDisplayName = null))
        return File(partialRoot, "${cacheKey(destination)}$suffix.part").canonicalFile.absolutePath
    }

    override fun resolveDisplayName(
        destination: String,
        explicitDisplayName: String?,
    ): String {
        explicitDisplayName?.takeIf(String::isNotBlank)?.let { return it }
        if (!isContentDestination(destination)) {
            return LocalFileDownloadDestinationAccess.resolveDisplayName(
                destination = destination,
                explicitDisplayName = null,
            )
        }

        return Uri.parse(destination).lastPathSegment
            ?.substringAfterLast('/')
            ?.takeIf(String::isNotBlank)
            ?: "download-${cacheKey(destination).take(8)}"
    }

    override fun prepareDestination(destination: String, tempFile: File) {
        ensureParentDirectory(tempFile)
        if (!isContentDestination(destination)) {
            LocalFileDownloadDestinationAccess.prepareDestination(destination, tempFile)
        }
    }

    override fun exists(destination: String): Boolean {
        if (isContentDestination(destination)) {
            return false
        }
        return LocalFileDownloadDestinationAccess.exists(destination)
    }

    override fun delete(destination: String): Boolean {
        if (!isContentDestination(destination)) {
            return LocalFileDownloadDestinationAccess.delete(destination)
        }

        return runCatching {
            contentResolver.delete(Uri.parse(destination), null, null)
            true
        }.getOrDefault(false)
    }

    override fun finalizeDownload(
        tempFile: File,
        destination: String,
        overwriteExisting: Boolean,
    ) {
        if (!isContentDestination(destination)) {
            LocalFileDownloadDestinationAccess.finalizeDownload(
                tempFile = tempFile,
                destination = destination,
                overwriteExisting = overwriteExisting,
            )
            return
        }

        val targetUri = Uri.parse(destination)
        try {
            tempFile.inputStream().use { input ->
                openWritableStream(targetUri).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            publishMediaStoreItem(targetUri)
        } catch (error: SecurityException) {
            throw IOException(error.message ?: "Permission denied for $destination", error)
        }

        tempFile.delete()
    }

    private fun openWritableStream(uri: Uri) =
        listOf("rwt", "wt", "w").firstNotNullOfOrNull { mode ->
            runCatching {
                contentResolver.openOutputStream(uri, mode)
            }.getOrNull()
        } ?: throw IOException("Failed to open output stream for $uri")

    private fun extensionSuffix(displayName: String): String {
        val extension = displayName.substringAfterLast('.', "")
            .takeIf(String::isNotBlank)
            ?: return ""
        return ".$extension"
    }

    private fun ensureParentDirectory(file: File) {
        val parent = file.parentFile ?: return
        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("Failed to create directory ${parent.absolutePath}")
        }
    }

    private fun cacheKey(destination: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(destination.encodeToByteArray())
        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }

    private fun publishMediaStoreItem(uri: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !isMediaStoreDestination(uri)) {
            return
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
        runCatching {
            contentResolver.update(uri, values, null, null)
        }
    }

    private fun isMediaStoreDestination(uri: Uri): Boolean {
        return uri.scheme.equals(other = "content", ignoreCase = true) &&
            uri.authority.equals(other = MediaStore.AUTHORITY, ignoreCase = true)
    }

    private fun isContentDestination(destination: String): Boolean =
        destination.startsWith(prefix = "content://", ignoreCase = true)
}
