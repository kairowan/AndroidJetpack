package com.ghn.lib.upload.bridge

import com.ghn.lib.upload.UploadRequest
import com.ghn.lib.upload.UploadSnapshot
import com.ghn.lib.upload.picker.PickedUploadFile
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
 * 描述: 上传桥接载荷工具，负责处理上传协议的解析与回传数据映射。
 */
data class UploadBridgeCommand(
    val id: String,
)

data class UploadBridgeSubscription(
    val id: String,
    val callbackHandler: String,
)

fun String?.toUploadRequestOrNull(): UploadRequest? {
    if (this.isNullOrBlank()) {
        return null
    }
    return runCatching {
        // 上传桥接统一走 JSON 协议，先解析为宿主可直接消费的请求模型。
        val json = JSONObject(this)
        val url = json.optString("url").takeIf(String::isNotBlank) ?: return null
        val fileUri = json.optString("fileUri").takeIf(String::isNotBlank) ?: return null
        UploadRequest(
            url = url,
            fileUri = fileUri,
            fileName = json.optString("fileName").ifBlank { null },
            fieldName = json.optString("fieldName").ifBlank { "file" },
            mimeType = json.optString("mimeType").ifBlank { null },
            method = json.optString("method").ifBlank { "POST" },
            headers = json.optStringMap("headers"),
            formFields = json.optStringMap("formFields"),
            tag = json.optString("tag").ifBlank { null },
            group = json.optString("group").ifBlank { null },
            extras = json.optStringMap("extras"),
            maxRetries = json.optInt("maxRetries", 1),
            priority = json.optInt("priority", 0),
        )
    }.getOrNull()
}

fun String?.toUploadBridgeCommandOrNull(): UploadBridgeCommand? {
    if (this.isNullOrBlank()) {
        return null
    }
    return runCatching {
        if (!startsWith("{")) {
            return UploadBridgeCommand(id = trim())
        }
        val json = JSONObject(this)
        val id = json.optString("id")
            .ifBlank { json.optString("taskId") }
            .ifBlank { null }
            ?: return null
        UploadBridgeCommand(id = id)
    }.getOrNull()
}

fun String?.toUploadBridgeSubscriptionOrNull(): UploadBridgeSubscription? {
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
        UploadBridgeSubscription(
            id = id,
            callbackHandler = callbackHandler
        )
    }.getOrNull()
}

fun String?.toUploadPickerMimeTypes(): Array<String> {
    if (this.isNullOrBlank()) {
        return arrayOf("*/*")
    }
    return runCatching {
        if (!startsWith("{")) {
            return arrayOf(this.trim())
        }
        val json = JSONObject(this)
        val mimeTypes = json.optJSONArray("mimeTypes")
            ?.let { array ->
                buildList {
                    for (index in 0 until array.length()) {
                        array.optString(index)
                            ?.takeIf(String::isNotBlank)
                            ?.let(::add)
                    }
                }
            }
            .orEmpty()
        when {
            mimeTypes.isNotEmpty() -> mimeTypes.toTypedArray()
            json.optString("mimeType").isNotBlank() -> arrayOf(json.optString("mimeType"))
            json.optString("accept").isNotBlank() -> arrayOf(json.optString("accept"))
            else -> arrayOf("*/*")
        }
    }.getOrElse {
        arrayOf("*/*")
    }
}

fun UploadSnapshot.toUploadBridgePayload(): Map<String, Any?> {
    return mapOf(
        "id" to id.value,
        "url" to url,
        "fileUri" to fileUri,
        "fileName" to fileName,
        "status" to status.name.lowercase(),
        "progressPercent" to progressPercent,
        "bytesUploaded" to bytesUploaded,
        "totalBytes" to totalBytes,
        "responseCode" to responseCode,
        "responseBody" to responseBody,
        "errorMessage" to errorMessage,
        "retryCount" to retryCount,
        "maxRetries" to maxRetries,
        "updatedAtEpochMs" to updatedAtEpochMs
    )
}

fun PickedUploadFile.toUploadBridgePayload(): Map<String, Any?> {
    return mapOf(
        "uri" to uri,
        "displayName" to displayName,
        "mimeType" to mimeType,
        "sizeBytes" to sizeBytes
    )
}

private fun JSONObject.optStringMap(name: String): Map<String, String> {
    val jsonObject = optJSONObject(name) ?: return emptyMap()
    return buildMap {
        val iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            put(key, jsonObject.optString(key))
        }
    }
}
