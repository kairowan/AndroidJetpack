@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.kotlinmvvm.core.data.eyepetizer

import com.kotlinmvvm.core.data.repository.EyepetizerPayloadParseException
import platform.Foundation.NSData
import platform.Foundation.NSJSONReadingFragmentsAllowed
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSNumber

internal fun NSData.toPayloadResponse(requestUrl: String): EyepetizerPayloadResponse {
    val jsonObject = runCatching {
        NSJSONSerialization.JSONObjectWithData(
            data = this@toPayloadResponse,
            options = NSJSONReadingFragmentsAllowed,
            error = null
        )
    }.getOrElse { error ->
        throw EyepetizerPayloadParseException(
            detail = "${error.message ?: "unknown parser error"} ($requestUrl)",
            cause = error
        )
    } ?: throw EyepetizerPayloadParseException(
        detail = "empty json object ($requestUrl)"
    )
    return jsonObject.toPayloadResponse()
}

private fun Any?.toPayloadResponse(): EyepetizerPayloadResponse {
    val payload = when (this) {
        is Map<*, *> -> this
        else -> emptyMap<String, Any?>()
    }
    return EyepetizerPayloadResponse(
        itemList = payload.listValue("itemList").mapNotNull { item -> item.toPayloadItem() },
        nextPageUrl = payload.stringValue("nextPageUrl")
    )
}

private fun Any?.toPayloadItem(): EyepetizerPayloadItem? {
    val payload = this as? Map<*, *> ?: return null
    return EyepetizerPayloadItem(
        type = payload.stringValue("type"),
        data = payload.mapValue("data")?.toPayloadData()
    )
}

private fun Map<*, *>.toPayloadData(): EyepetizerPayloadData {
    return EyepetizerPayloadData(
        dataType = stringValue("dataType"),
        id = intValue("id"),
        title = stringValue("title"),
        description = stringValue("description"),
        text = stringValue("text"),
        playUrl = stringValue("playUrl"),
        duration = intValue("duration"),
        category = stringValue("category"),
        cover = mapValue("cover")?.toPayloadCover(),
        author = mapValue("author")?.toPayloadAuthor(),
        header = mapValue("header")?.toPayloadHeader(),
        itemList = listValue("itemList").mapNotNull { item -> item.toPayloadItem() }
    )
}

private fun Map<*, *>.toPayloadCover(): EyepetizerPayloadCover {
    return EyepetizerPayloadCover(
        feed = stringValue("feed"),
        detail = stringValue("detail")
    )
}

private fun Map<*, *>.toPayloadAuthor(): EyepetizerPayloadAuthor {
    return EyepetizerPayloadAuthor(
        name = stringValue("name"),
        icon = stringValue("icon")
    )
}

private fun Map<*, *>.toPayloadHeader(): EyepetizerPayloadHeader {
    return EyepetizerPayloadHeader(title = stringValue("title"))
}

private fun Map<*, *>.stringValue(key: String): String? {
    return this[key] as? String
}

private fun Map<*, *>.intValue(key: String): Int? {
    return when (val value = this[key]) {
        is Int -> value
        is Long -> value.toInt()
        is Double -> value.toInt()
        is Float -> value.toInt()
        is NSNumber -> value.intValue.toInt()
        else -> null
    }
}

private fun Map<*, *>.mapValue(key: String): Map<*, *>? {
    return this[key] as? Map<*, *>
}

private fun Map<*, *>.listValue(key: String): List<*> {
    return this[key] as? List<*> ?: emptyList<Any?>()
}
