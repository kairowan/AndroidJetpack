package com.example.flowdownload

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.flowdownload.download.model.DownloadConflictPolicy
import com.example.flowdownload.download.model.DownloadConstraints
import com.example.flowdownload.download.model.DownloadId
import com.example.flowdownload.download.model.DownloadRequest
import com.example.flowdownload.download.storage.MediaStoreDownloadRequestFactory
import com.example.flowdownload.download.storage.MediaStoreExistingLookupSpec
import com.example.flowdownload.download.storage.MediaStoreDownloadRequestResolver
import com.example.flowdownload.download.storage.MediaStoreDownloadRequestSpec
import java.io.IOException

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: MediaStore 便捷请求构建器，负责为下载任务预创建 Downloads、Images、Video、Audio 集合中的 content Uri 目标。
 */
object FlowDownloadMediaStore {
    const val DEFAULT_VOLUME_NAME: String = "external_primary"
    private const val DIRECTORY_DOWNLOADS = "Download"
    private const val DIRECTORY_PICTURES = "Pictures"
    private const val DIRECTORY_MOVIES = "Movies"
    private const val DIRECTORY_MUSIC = "Music"

    @Throws(IOException::class)
    fun createRequests(
        context: Context,
        specs: kotlin.collections.Collection<RequestSpec>,
    ): List<DownloadRequest> {
        val factory = createFactory(context)
        return factory.createRequests(
            specs = specs.map(RequestSpec::toInternalSpec),
        )
    }

    @Throws(IOException::class)
    fun findExisting(
        context: Context,
        collection: Collection,
        displayName: String,
        relativePath: String? = null,
        volumeName: String = DEFAULT_VOLUME_NAME,
    ): List<Uri> {
        val factory = createFactory(context)
        return factory.findExistingDestinations(
            spec = MediaStoreExistingLookupSpec(
                collection = collection,
                displayName = displayName,
                relativePath = relativePath,
                volumeName = volumeName,
            ),
        ).map(Uri::parse)
    }

    @Throws(IOException::class)
    fun deleteExisting(
        context: Context,
        collection: Collection,
        displayName: String,
        relativePath: String? = null,
        volumeName: String = DEFAULT_VOLUME_NAME,
    ): Int {
        val factory = createFactory(context)
        return factory.deleteExistingDestinations(
            spec = MediaStoreExistingLookupSpec(
                collection = collection,
                displayName = displayName,
                relativePath = relativePath,
                volumeName = volumeName,
            ),
        )
    }

    @Throws(IOException::class)
    fun createRequest(
        context: Context,
        url: String,
        displayName: String,
        collection: Collection,
        relativePath: String? = null,
        mimeType: String? = null,
        tag: String? = null,
        group: String? = null,
        extras: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        constraints: DownloadConstraints = DownloadConstraints(),
        expectedSha256: String? = null,
        conflictPolicy: DownloadConflictPolicy = DownloadConflictPolicy.REJECT,
        overwriteExisting: Boolean = true,
        maxRetries: Int = 2,
        priority: Int = 0,
        tempFilePath: String? = null,
        volumeName: String = DEFAULT_VOLUME_NAME,
        id: DownloadId = DownloadId.newId(),
    ): DownloadRequest {
        val factory = createFactory(context)
        return factory.createRequest(
            spec = MediaStoreDownloadRequestSpec(
                id = id,
                url = url,
                displayName = displayName,
                collection = collection,
                relativePath = relativePath,
                mimeType = mimeType,
                tag = tag,
                group = group,
                extras = extras,
                headers = headers,
                constraints = constraints,
                expectedSha256 = expectedSha256,
                conflictPolicy = conflictPolicy,
                overwriteExisting = overwriteExisting,
                maxRetries = maxRetries,
                priority = priority,
                tempFilePath = tempFilePath,
                volumeName = volumeName,
            ),
        )
    }

    @Throws(IOException::class)
    fun downloads(
        context: Context,
        url: String,
        displayName: String,
        relativePath: String? = null,
        mimeType: String? = null,
        tag: String? = null,
        group: String? = null,
        extras: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        constraints: DownloadConstraints = DownloadConstraints(),
        expectedSha256: String? = null,
        conflictPolicy: DownloadConflictPolicy = DownloadConflictPolicy.REJECT,
        overwriteExisting: Boolean = true,
        maxRetries: Int = 2,
        priority: Int = 0,
        tempFilePath: String? = null,
        volumeName: String = DEFAULT_VOLUME_NAME,
        id: DownloadId = DownloadId.newId(),
    ): DownloadRequest {
        return createRequest(
            context = context,
            url = url,
            displayName = displayName,
            collection = Collection.DOWNLOADS,
            relativePath = relativePath,
            mimeType = mimeType,
            tag = tag,
            group = group,
            extras = extras,
            headers = headers,
            constraints = constraints,
            expectedSha256 = expectedSha256,
            conflictPolicy = conflictPolicy,
            overwriteExisting = overwriteExisting,
            maxRetries = maxRetries,
            priority = priority,
            tempFilePath = tempFilePath,
            volumeName = volumeName,
            id = id,
        )
    }

    @Throws(IOException::class)
    fun images(
        context: Context,
        url: String,
        displayName: String,
        relativePath: String? = null,
        mimeType: String? = null,
        tag: String? = null,
        group: String? = null,
        extras: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        constraints: DownloadConstraints = DownloadConstraints(),
        expectedSha256: String? = null,
        conflictPolicy: DownloadConflictPolicy = DownloadConflictPolicy.REJECT,
        overwriteExisting: Boolean = true,
        maxRetries: Int = 2,
        priority: Int = 0,
        tempFilePath: String? = null,
        volumeName: String = DEFAULT_VOLUME_NAME,
        id: DownloadId = DownloadId.newId(),
    ): DownloadRequest {
        return createRequest(
            context = context,
            url = url,
            displayName = displayName,
            collection = Collection.IMAGES,
            relativePath = relativePath,
            mimeType = mimeType,
            tag = tag,
            group = group,
            extras = extras,
            headers = headers,
            constraints = constraints,
            expectedSha256 = expectedSha256,
            conflictPolicy = conflictPolicy,
            overwriteExisting = overwriteExisting,
            maxRetries = maxRetries,
            priority = priority,
            tempFilePath = tempFilePath,
            volumeName = volumeName,
            id = id,
        )
    }

    @Throws(IOException::class)
    fun videos(
        context: Context,
        url: String,
        displayName: String,
        relativePath: String? = null,
        mimeType: String? = null,
        tag: String? = null,
        group: String? = null,
        extras: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        constraints: DownloadConstraints = DownloadConstraints(),
        expectedSha256: String? = null,
        conflictPolicy: DownloadConflictPolicy = DownloadConflictPolicy.REJECT,
        overwriteExisting: Boolean = true,
        maxRetries: Int = 2,
        priority: Int = 0,
        tempFilePath: String? = null,
        volumeName: String = DEFAULT_VOLUME_NAME,
        id: DownloadId = DownloadId.newId(),
    ): DownloadRequest {
        return createRequest(
            context = context,
            url = url,
            displayName = displayName,
            collection = Collection.VIDEOS,
            relativePath = relativePath,
            mimeType = mimeType,
            tag = tag,
            group = group,
            extras = extras,
            headers = headers,
            constraints = constraints,
            expectedSha256 = expectedSha256,
            conflictPolicy = conflictPolicy,
            overwriteExisting = overwriteExisting,
            maxRetries = maxRetries,
            priority = priority,
            tempFilePath = tempFilePath,
            volumeName = volumeName,
            id = id,
        )
    }

    @Throws(IOException::class)
    fun audio(
        context: Context,
        url: String,
        displayName: String,
        relativePath: String? = null,
        mimeType: String? = null,
        tag: String? = null,
        group: String? = null,
        extras: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        constraints: DownloadConstraints = DownloadConstraints(),
        expectedSha256: String? = null,
        conflictPolicy: DownloadConflictPolicy = DownloadConflictPolicy.REJECT,
        overwriteExisting: Boolean = true,
        maxRetries: Int = 2,
        priority: Int = 0,
        tempFilePath: String? = null,
        volumeName: String = DEFAULT_VOLUME_NAME,
        id: DownloadId = DownloadId.newId(),
    ): DownloadRequest {
        return createRequest(
            context = context,
            url = url,
            displayName = displayName,
            collection = Collection.AUDIO,
            relativePath = relativePath,
            mimeType = mimeType,
            tag = tag,
            group = group,
            extras = extras,
            headers = headers,
            constraints = constraints,
            expectedSha256 = expectedSha256,
            conflictPolicy = conflictPolicy,
            overwriteExisting = overwriteExisting,
            maxRetries = maxRetries,
            priority = priority,
            tempFilePath = tempFilePath,
            volumeName = volumeName,
            id = id,
        )
    }

    /**
     * @author 浩楠
     * @date 2026/6/9
     *      _              _           _     _   ____  _             _ _
     *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
     *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
     *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
     *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
     *  描述: MediaStore 目标集合枚举，负责定义默认目录和不同媒体集合对应的 collection Uri。
     */
    enum class Collection(
        val defaultRelativeDirectory: String,
    ) {
        DOWNLOADS(DIRECTORY_DOWNLOADS) {
            override fun contentUri(volumeName: String): Uri = MediaStore.Downloads.getContentUri(volumeName)
        },
        IMAGES(DIRECTORY_PICTURES) {
            override fun contentUri(volumeName: String): Uri = MediaStore.Images.Media.getContentUri(volumeName)
        },
        VIDEOS(DIRECTORY_MOVIES) {
            override fun contentUri(volumeName: String): Uri = MediaStore.Video.Media.getContentUri(volumeName)
        },
        AUDIO(DIRECTORY_MUSIC) {
            override fun contentUri(volumeName: String): Uri = MediaStore.Audio.Media.getContentUri(volumeName)
        },
        ;

        internal abstract fun contentUri(volumeName: String): Uri
    }

    /**
     * @author 浩楠
     * @date 2026/6/9
     *      _              _           _     _   ____  _             _ _
     *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
     *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
     *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
     *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
     *  描述: MediaStore 批量请求参数模型，供宿主一次性声明多个目标集合、目录和通用下载字段。
     */
    data class RequestSpec(
        val url: String,
        val displayName: String,
        val collection: Collection,
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
        val volumeName: String = DEFAULT_VOLUME_NAME,
        val id: DownloadId = DownloadId.newId(),
    ) {
        internal fun toInternalSpec(): MediaStoreDownloadRequestSpec {
            return MediaStoreDownloadRequestSpec(
                id = id,
                url = url,
                displayName = displayName,
                collection = collection,
                relativePath = relativePath,
                mimeType = mimeType,
                tag = tag,
                group = group,
                extras = extras,
                headers = headers,
                constraints = constraints,
                expectedSha256 = expectedSha256,
                conflictPolicy = conflictPolicy,
                overwriteExisting = overwriteExisting,
                maxRetries = maxRetries,
                priority = priority,
                tempFilePath = tempFilePath,
                volumeName = volumeName,
            )
        }
    }

    private fun createFactory(context: Context): MediaStoreDownloadRequestFactory {
        return MediaStoreDownloadRequestFactory(
            resolver = AndroidMediaStoreDownloadRequestResolver(
                context = context.applicationContext,
            ),
        )
    }
}

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: Android MediaStore 请求解析器，负责查询同名条目、预插入 pending 行并返回 content Uri 字符串。
 */
private class AndroidMediaStoreDownloadRequestResolver(
    context: Context,
) : MediaStoreDownloadRequestResolver {
    private val contentResolver: ContentResolver = context.contentResolver

    override val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun findExistingDestinations(
        collection: FlowDownloadMediaStore.Collection,
        volumeName: String,
        displayName: String,
        relativePath: String,
    ): List<String> {
        val destinations = mutableListOf<String>()
        val uri = collection.contentUri(volumeName)
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND " +
            "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(displayName, relativePath)
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                destinations += Uri.withAppendedPath(uri, id.toString()).toString()
            }
        }
        return destinations
    }

    override fun deleteDestination(destination: String): Boolean {
        return runCatching {
            contentResolver.delete(Uri.parse(destination), null, null) > 0
        }.getOrDefault(false)
    }

    override fun createDestination(
        collection: FlowDownloadMediaStore.Collection,
        volumeName: String,
        displayName: String,
        relativePath: String,
        mimeType: String?,
    ): String? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
            mimeType?.let { put(MediaStore.MediaColumns.MIME_TYPE, it) }
        }
        return contentResolver.insert(collection.contentUri(volumeName), values)?.toString()
    }
}
