package com.ghn.lib.upload.picker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.ghn.lib.upload.UploadSourceResolver
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
 * 描述: 上传选择器宿主 Fragment，负责承接系统文件选择回调并转换为挂起式调用。
 */
data class PickedUploadFile(
    val uri: String,
    val displayName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
) {
    companion object {
        fun from(
            context: Context,
            uri: Uri,
        ): PickedUploadFile {
            val source = uri.toString()
            return PickedUploadFile(
                uri = source,
                displayName = UploadSourceResolver.resolveDisplayName(context, source, null),
                mimeType = UploadSourceResolver.resolveMimeType(context, source, null),
                sizeBytes = UploadSourceResolver.contentLength(context, source),
            )
        }
    }
}

class UploadPickerHostFragment : Fragment() {
    private var pendingSingleRequest: CancellableContinuation<PickedUploadFile?>? = null
    private var pendingMultiRequest: CancellableContinuation<List<Uri>>? = null

    private val singleDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val continuation = pendingSingleRequest ?: return@registerForActivityResult
        pendingSingleRequest = null
        continuation.resume(uri?.let(::persistAndMap))
    }

    private val multiDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val continuation = pendingMultiRequest ?: return@registerForActivityResult
        pendingMultiRequest = null
        continuation.resume(uris.map(::persistUri))
    }

    suspend fun pickImage(): PickedUploadFile? {
        return pickSingle(arrayOf("image/*"))
    }

    suspend fun pickFile(mimeTypes: Array<String>): PickedUploadFile? {
        return pickSingle(normalizeMimeTypes(mimeTypes))
    }

    suspend fun pickWebFiles(
        acceptTypes: Array<String>,
        allowMultiple: Boolean,
    ): List<Uri> {
        val normalizedTypes = normalizeMimeTypes(acceptTypes)
        return if (allowMultiple) {
            pickMultiple(normalizedTypes)
        } else {
            pickSingle(normalizedTypes)
                ?.let { listOf(Uri.parse(it.uri)) }
                .orEmpty()
        }
    }

    private suspend fun pickSingle(mimeTypes: Array<String>): PickedUploadFile? {
        ensureNoPendingRequest()
        return suspendCancellableCoroutine { continuation ->
            pendingSingleRequest = continuation
            continuation.invokeOnCancellation {
                if (pendingSingleRequest === continuation) {
                    pendingSingleRequest = null
                }
            }
            runCatching {
                // 通过挂起包装 ActivityResult，避免 WebBridge 或上传流程侧继续维护回调地狱。
                singleDocumentLauncher.launch(mimeTypes)
            }.onFailure { throwable ->
                if (pendingSingleRequest === continuation) {
                    pendingSingleRequest = null
                }
                continuation.resumeWithException(throwable)
            }
        }
    }

    private suspend fun pickMultiple(mimeTypes: Array<String>): List<Uri> {
        ensureNoPendingRequest()
        return suspendCancellableCoroutine { continuation ->
            pendingMultiRequest = continuation
            continuation.invokeOnCancellation {
                if (pendingMultiRequest === continuation) {
                    pendingMultiRequest = null
                }
            }
            runCatching {
                multiDocumentLauncher.launch(mimeTypes)
            }.onFailure { throwable ->
                if (pendingMultiRequest === continuation) {
                    pendingMultiRequest = null
                }
                continuation.resumeWithException(throwable)
            }
        }
    }

    private fun ensureNoPendingRequest() {
        check(pendingSingleRequest == null && pendingMultiRequest == null) {
            "another picker request is still in progress"
        }
    }

    private fun persistAndMap(uri: Uri): PickedUploadFile {
        return PickedUploadFile.from(requireContext(), persistUri(uri))
    }

    private fun persistUri(uri: Uri): Uri {
        runCatching {
            // 持久化读权限，避免后续上传发生在页面返回后时已经失去 Uri 访问能力。
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        return uri
    }

    companion object {
        private const val TAG = "UploadPickerHostFragment"

        fun obtain(activity: FragmentActivity): UploadPickerHostFragment {
            activity.supportFragmentManager.findFragmentByTag(TAG)
                ?.let { existing ->
                    return existing as UploadPickerHostFragment
                }
            val fragment = UploadPickerHostFragment()
            activity.supportFragmentManager.beginTransaction()
                .add(fragment, TAG)
                .commitNowAllowingStateLoss()
            return fragment
        }

        private fun normalizeMimeTypes(mimeTypes: Array<String>): Array<String> {
            val normalized = mimeTypes
                .asSequence()
                .flatMap { raw ->
                    raw.split(',')
                        .asSequence()
                }
                .map(String::trim)
                .mapNotNull { value ->
                    when {
                        value.isBlank() -> null
                        value.startsWith('.') -> MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(value.removePrefix(".").lowercase())

                        else -> value
                    }
                }
                .distinct()
                .toList()
            return if (normalized.isEmpty()) {
                arrayOf("*/*")
            } else {
                normalized.toTypedArray()
            }
        }
    }
}
