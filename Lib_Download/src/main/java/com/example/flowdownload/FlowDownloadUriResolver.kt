package com.example.flowdownload

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.flowdownload.download.model.DownloadSnapshot
import java.io.File

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载结果 Uri 解析扩展点，允许宿主接管文件路径到可分享 Uri 的映射策略。
 */
fun interface FlowDownloadUriResolver {
    fun resolve(
        context: Context,
        snapshot: DownloadSnapshot,
    ): Uri
}

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 默认下载结果 Uri 解析器，content:// 目标直接复用，文件路径目标通过库内置 FileProvider 暴露只读 Uri。
 */
object DefaultFlowDownloadUriResolver : FlowDownloadUriResolver {
    const val AUTHORITY_SUFFIX: String = ".flowdownload.fileprovider"

    override fun resolve(
        context: Context,
        snapshot: DownloadSnapshot,
    ): Uri {
        val destination = snapshot.destination.trim()
        if (destination.startsWith(prefix = "content://", ignoreCase = true)) {
            return Uri.parse(destination)
        }

        val file = File(destination)
        require(file.exists()) {
            "Downloaded file does not exist: ${snapshot.destination}"
        }

        return runCatching {
            FileProvider.getUriForFile(context, authority(context), file)
        }.getOrElse { error ->
            throw IllegalArgumentException(
                "Failed to resolve Uri for ${snapshot.destination}. " +
                    "If you use a custom storage root, provide your own FlowDownloadUriResolver.",
                error,
            )
        }
    }

    fun authority(context: Context): String = context.packageName + AUTHORITY_SUFFIX
}
