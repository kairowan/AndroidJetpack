package com.ghn.lib.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 上传源解析器，负责解析 Uri 对应的文件元信息与输入源。
 */

object UploadSourceResolver {

    fun resolveUri(source: String): Uri {
        return when {
            source.startsWith("content://") || source.startsWith("file://") -> Uri.parse(source)
            else -> Uri.fromFile(File(source))
        }
    }

    fun resolveDisplayName(
        context: Context,
        source: String,
        fallback: String?,
    ): String {
        fallback
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.let { return it }
        val uri = resolveUri(source)
        if (uri.scheme == "content") {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getString(0)
                }
            }
        }
        return uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.takeIf(String::isNotBlank)
            ?: "upload_${System.currentTimeMillis()}"
    }

    fun resolveMimeType(
        context: Context,
        source: String,
        fallback: String?,
    ): String? {
        fallback
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.let { return it }
        val uri = resolveUri(source)
        return when (uri.scheme) {
            "content" -> context.contentResolver.getType(uri)
            else -> null
        }
    }

    fun contentLength(
        context: Context,
        source: String,
    ): Long? {
        val uri = resolveUri(source)
        return when (uri.scheme) {
            "content" -> {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.SIZE),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val size = cursor.getLong(0)
                        if (size >= 0L) {
                            return size
                        }
                    }
                }
                context.contentResolver.openAssetFileDescriptor(uri, "r")
                    ?.use { descriptor ->
                        descriptor.length.takeIf { it >= 0L }
                    }
            }

            else -> File(uri.path ?: source).takeIf(File::exists)?.length()
        }
    }

    @Throws(IOException::class)
    fun openInputStream(
        context: Context,
        source: String,
    ): InputStream {
        val uri = resolveUri(source)
        return when (uri.scheme) {
            "content" -> context.contentResolver.openInputStream(uri)
            "file" -> uri.path?.let(::File)?.inputStream()
            else -> File(source).inputStream()
        } ?: throw IOException("Unable to open upload source: $source")
    }
}
