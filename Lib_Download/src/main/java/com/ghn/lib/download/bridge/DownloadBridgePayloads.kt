package com.ghn.lib.download.bridge

import com.example.flowdownload.download.model.DownloadEnqueueResult
import com.example.flowdownload.download.model.DownloadSnapshot
import com.ghn.lib.download.DownloadTaskRequest
import org.json.JSONObject

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
 * 描述: 下载桥接载荷工具，负责处理下载协议的解析与回传数据映射。
 */
data class DownloadBridgeCommand(
    val id: String,
    val deletePartialFile: Boolean = false,
)

data class DownloadBridgeSubscription(
    val id: String,
    val callbackHandler: String,
)

fun String?.toDownloadTaskRequestOrNull(): DownloadTaskRequest? {
    if (this.isNullOrBlank()) {
        return null
    }
    if (startsWith("http://") || startsWith("https://")) {
        return DownloadTaskRequest(url = this)
    }
    return runCatching {
        // Web 侧允许直接传 url，也允许传完整 JSON，这里统一折叠成宿主下载请求模型。
        val json = JSONObject(this)
        val url = json.optString("url").takeIf(String::isNotBlank) ?: return null
        DownloadTaskRequest(
            url = url,
            displayName = json.optString("displayName")
                .ifBlank { json.optString("fileName") }
                .ifBlank { null },
            subDirectory = json.optString("subDirectory").ifBlank { null },
            mimeType = json.optString("mimeType").ifBlank { null },
            headers = json.optStringMap("headers"),
            tag = json.optString("tag").ifBlank { null },
            group = json.optString("group").ifBlank { null },
            extras = json.optStringMap("extras"),
            overwriteExisting = json.optBoolean("overwriteExisting", true),
            preferPublicDownloads = json.optBoolean("preferPublicDownloads", true),
            maxRetries = json.optInt("maxRetries", 2),
            priority = json.optInt("priority", 0),
        )
    }.getOrNull()
}

fun String?.toDownloadBridgeCommandOrNull(): DownloadBridgeCommand? {
    if (this.isNullOrBlank()) {
        return null
    }
    return runCatching {
        if (!startsWith("{")) {
            return DownloadBridgeCommand(id = trim())
        }
        val json = JSONObject(this)
        val id = json.optString("id")
            .ifBlank { json.optString("taskId") }
            .ifBlank { null }
            ?: return null
        DownloadBridgeCommand(
            id = id,
            deletePartialFile = json.optBoolean("deletePartialFile", false)
        )
    }.getOrNull()
}

fun String?.toDownloadBridgeSubscriptionOrNull(): DownloadBridgeSubscription? {
    if (this.isNullOrBlank() || !startsWith("{")) {
        return null
    }
    return runCatching {
        val json = JSONObject(this)
        val id = json.optString("id")
            .ifBlank { json.optString("taskId") }
            .ifBlank { null }
            ?: return null
        val callbackHandler = json.optString("callbackHandler")
            .ifBlank { json.optString("handlerName") }
            .ifBlank { json.optString("eventHandler") }
            .ifBlank { null }
            ?: return null
        DownloadBridgeSubscription(
            id = id,
            callbackHandler = callbackHandler
        )
    }.getOrNull()
}

fun DownloadEnqueueResult.toDownloadBridgePayload(): Map<String, Any?> {
    return when (this) {
        is DownloadEnqueueResult.Created -> mapOf(
            "type" to "created",
            "id" to downloadId.value
        )

        is DownloadEnqueueResult.ReusedExisting -> mapOf(
            "type" to "reused",
            "id" to downloadId.value,
            "snapshot" to existingSnapshot.toDownloadBridgePayload()
        )

        is DownloadEnqueueResult.ReplacedExisting -> mapOf(
            "type" to "replaced",
            "id" to downloadId.value,
            "replacedId" to replacedDownloadId.value
        )
    }
}

fun DownloadSnapshot.toDownloadBridgePayload(): Map<String, Any?> {
    return mapOf(
        "id" to id.value,
        "url" to url,
        "destination" to destination,
        "displayName" to displayName,
        "tag" to tag,
        "group" to group,
        "status" to status::class.java.simpleName,
        "progressPercent" to progressPercent,
        "bytesDownloaded" to bytesDownloaded,
        "totalBytes" to totalBytes,
        "retryCount" to retryCount,
        "maxRetries" to maxRetries,
        "updatedAtEpochMs" to updatedAtEpochMs
    )
}

private fun JSONObject.optStringMap(name: String): Map<String, String> {
    val jsonObject = optJSONObject(name) ?: return emptyMap()
    return buildMap {
        // 统一把嵌套对象拉平为字符串字典，便于桥接层与前端协议对齐。
        val iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            put(key, jsonObject.optString(key))
        }
    }
}
