package com.example.flowdownload.download.storage

import com.example.flowdownload.FlowDownloadMediaStore
import com.example.flowdownload.download.model.DownloadConflictPolicy
import com.example.flowdownload.download.model.DownloadConstraints
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadRequest
import java.io.IOException

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: MediaStore 下载请求工厂，负责规范化目标路径、处理同名条目冲突并产出可直接入队的 DownloadRequest。
 */
internal class MediaStoreDownloadRequestFactory(
    private val resolver: MediaStoreDownloadRequestResolver,
) {
    @Throws(IOException::class)
    fun createRequests(
        specs: Collection<MediaStoreDownloadRequestSpec>,
    ): List<DownloadRequest> {
        if (specs.isEmpty()) {
            return emptyList()
        }

        val seenTargets = linkedSetOf<MediaStoreBatchTargetKey>()
        specs.forEach { spec ->
            val displayName = normalizeDisplayName(spec.displayName)
            val relativePath = normalizeRelativePath(
                relativePath = spec.relativePath,
                defaultDirectory = spec.collection.defaultRelativeDirectory,
            )
            val targetKey = MediaStoreBatchTargetKey(
                collection = spec.collection,
                volumeName = spec.volumeName,
                displayName = displayName,
                relativePath = relativePath,
            )
            require(seenTargets.add(targetKey)) {
                "Duplicate MediaStore batch target: ${relativePath}${displayName}"
            }
        }

        val createdDestinations = mutableListOf<String>()
        return try {
            specs.map { spec ->
                createRequest(spec).also { request ->
                    createdDestinations += request.destination
                }
            }
        } catch (error: Throwable) {
            createdDestinations.forEach { destination ->
                runCatching {
                    resolver.deleteDestination(destination)
                }
            }
            throw error
        }
    }

    @Throws(IOException::class)
    fun createRequest(spec: MediaStoreDownloadRequestSpec): DownloadRequest {
        require(spec.url.isNotBlank()) {
            "Download url must not be blank"
        }

        val displayName = normalizeDisplayName(spec.displayName)

        if (!resolver.isSupported) {
            throw IOException("MediaStore request builders require Android 10 (API 29) or above")
        }

        val relativePath = normalizeRelativePath(
            relativePath = spec.relativePath,
            defaultDirectory = spec.collection.defaultRelativeDirectory,
        )
        val mimeType = spec.mimeType?.trim()?.takeIf(String::isNotBlank)
        val existingDestinations = findExistingDestinations(
            spec = MediaStoreExistingLookupSpec(
                collection = spec.collection,
                displayName = displayName,
                relativePath = relativePath,
                volumeName = spec.volumeName,
            ),
        )
        if (existingDestinations.isNotEmpty()) {
            if (!spec.overwriteExisting) {
                throw IllegalStateException(
                    "MediaStore item already exists at $relativePath$displayName",
                )
            }

            existingDestinations.forEach { destination ->
                if (!resolver.deleteDestination(destination)) {
                    throw IOException("Failed to replace existing MediaStore item: $destination")
                }
            }
        }

        val destination = resolver.createDestination(
            collection = spec.collection,
            volumeName = spec.volumeName,
            displayName = displayName,
            relativePath = relativePath,
            mimeType = mimeType,
        ) ?: throw IOException("Failed to create MediaStore destination for $displayName")

        return DownloadRequest(
            id = spec.id,
            url = spec.url,
            destination = destination,
            displayName = displayName,
            tag = spec.tag,
            group = spec.group,
            extras = spec.extras,
            headers = spec.headers,
            constraints = spec.constraints,
            expectedSha256 = spec.expectedSha256,
            conflictPolicy = spec.conflictPolicy,
            overwriteExisting = spec.overwriteExisting,
            maxRetries = spec.maxRetries,
            priority = spec.priority,
            tempFilePath = spec.tempFilePath,
        )
    }

    @Throws(IOException::class)
    fun findExistingDestinations(
        spec: MediaStoreExistingLookupSpec,
    ): List<String> {
        if (!resolver.isSupported) {
            throw IOException("MediaStore helpers require Android 10 (API 29) or above")
        }
        val displayName = normalizeDisplayName(spec.displayName)
        val relativePath = normalizeRelativePath(
            relativePath = spec.relativePath,
            defaultDirectory = spec.collection.defaultRelativeDirectory,
        )
        return resolver.findExistingDestinations(
            collection = spec.collection,
            volumeName = spec.volumeName,
            displayName = displayName,
            relativePath = relativePath,
        )
    }

    @Throws(IOException::class)
    fun deleteExistingDestinations(
        spec: MediaStoreExistingLookupSpec,
    ): Int {
        val destinations = findExistingDestinations(spec)
        destinations.forEach { destination ->
            if (!resolver.deleteDestination(destination)) {
                throw IOException("Failed to delete existing MediaStore item: $destination")
            }
        }
        return destinations.size
    }

    internal fun normalizeRelativePath(
        relativePath: String?,
        defaultDirectory: String,
    ): String {
        val raw = relativePath
            ?.replace('\\', '/')
            ?.trim()
            ?.trim('/')
            ?.takeIf(String::isNotBlank)
            ?: defaultDirectory
        return "$raw/"
    }

    internal fun normalizeDisplayName(displayName: String): String {
        val normalized = displayName.trim().takeIf(String::isNotBlank)
            ?: throw IllegalArgumentException("MediaStore displayName must not be blank")
        require(normalized.none { it == '/' || it == '\\' }) {
            "MediaStore displayName must not contain path separators"
        }
        return normalized
    }
}

/**
 * MediaStore 下载请求参数模型，封装集合、目录、媒体类型以及通用下载请求字段。
 */
internal data class MediaStoreDownloadRequestSpec(
    val id: DownloadId = DownloadId.newId(),
    val url: String,
    val displayName: String,
    val collection: FlowDownloadMediaStore.Collection,
    val relativePath: String? = null,
    val mimeType: String? = null,
    val tag: String? = null,
    val group: String? = null,
    val extras: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val constraints: DownloadConstraints = DownloadConstraints(),
    val expectedSha256: String? = null,
    val conflictPolicy: DownloadConflictPolicy = DownloadConflictPolicy.REJECT,
    val overwriteExisting: Boolean = true,
    val maxRetries: Int = 2,
    val priority: Int = 0,
    val tempFilePath: String? = null,
    val volumeName: String = FlowDownloadMediaStore.DEFAULT_VOLUME_NAME,
)

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: MediaStore 已有条目查询参数，用于按集合、目录和文件名查找或清理同名目标。
 */
internal data class MediaStoreExistingLookupSpec(
    val collection: FlowDownloadMediaStore.Collection,
    val displayName: String,
    val relativePath: String? = null,
    val volumeName: String = FlowDownloadMediaStore.DEFAULT_VOLUME_NAME,
)

/**
 * MediaStore 请求解析器抽象，便于 Android 实现与 JVM 单测替身复用同一套建模与冲突处理逻辑。
 */
internal interface MediaStoreDownloadRequestResolver {
    val isSupported: Boolean

    fun findExistingDestinations(
        collection: FlowDownloadMediaStore.Collection,
        volumeName: String,
        displayName: String,
        relativePath: String,
    ): List<String>

    fun deleteDestination(destination: String): Boolean

    fun createDestination(
        collection: FlowDownloadMediaStore.Collection,
        volumeName: String,
        displayName: String,
        relativePath: String,
        mimeType: String?,
    ): String?
}

private data class MediaStoreBatchTargetKey(
    val collection: FlowDownloadMediaStore.Collection,
    val volumeName: String,
    val displayName: String,
    val relativePath: String,
)
