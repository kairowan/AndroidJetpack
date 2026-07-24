@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.kotlinmvvm.shared.ios

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIViewContentMode
import platform.UIKit.setAccessibilityLabel
import platform.UIKit.setIsAccessibilityElement
import kotlin.coroutines.resume

/**
 * iOS 图片插槽：复用系统 URL cache，不额外引入图片框架。
 */
@Composable
internal fun IosRemoteImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var image by remember(url) { mutableStateOf<UIImage?>(null) }

    LaunchedEffect(url) {
        image = loadImage(url)
    }

    UIKitView(
        factory = {
            UIImageView().apply {
                clipsToBounds = true
                contentMode = UIViewContentMode.UIViewContentModeScaleAspectFill
                setAccessibilityLabel(contentDescription)
                setIsAccessibilityElement(true)
            }
        },
        modifier = modifier,
        update = { imageView ->
            imageView.image = image
            imageView.setAccessibilityLabel(contentDescription)
        }
    )
}

/**
 * AVPlayer 生命周期与 CMP 页面解耦；同一 Shorts 页面只复用一个播放器。
 */
internal class IosVideoPlayer {
    val nativePlayer = AVPlayer()

    private var currentUrl: String? = null

    fun play(url: String) {
        prepare(url)
        nativePlayer.play()
    }

    fun pause() {
        nativePlayer.pause()
    }

    fun toggle() {
        if (nativePlayer.rate() > 0f) pause() else nativePlayer.play()
    }

    fun release() {
        pause()
        nativePlayer.replaceCurrentItemWithPlayerItem(null)
        currentUrl = null
    }

    private fun prepare(url: String) {
        val normalizedUrl = url.atsSafeUrl()
        if (normalizedUrl == currentUrl) return
        val nativeUrl = NSURL.URLWithString(normalizedUrl) ?: return
        nativePlayer.replaceCurrentItemWithPlayerItem(AVPlayerItem(uRL = nativeUrl))
        currentUrl = normalizedUrl
    }
}

@Composable
internal fun IosVideoSurface(
    player: IosVideoPlayer,
    showPlaybackControls: Boolean,
    modifier: Modifier = Modifier
) {
    UIKitViewController(
        factory = {
            AVPlayerViewController().apply {
                this.player = player.nativePlayer
                showsPlaybackControls = showPlaybackControls
                videoGravity = AVLayerVideoGravityResizeAspectFill
            }
        },
        modifier = modifier,
        update = { controller ->
            controller.player = player.nativePlayer
            controller.showsPlaybackControls = showPlaybackControls
        },
        onRelease = { controller ->
            controller.player = null
        }
    )
}

private suspend fun loadImage(url: String): UIImage? = suspendCancellableCoroutine { continuation ->
    val nativeUrl = NSURL.URLWithString(url.atsSafeUrl())
    if (nativeUrl == null) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    val task = NSURLSession.sharedSession.dataTaskWithURL(nativeUrl) {
            data: NSData?,
            _,
            _ ->
        if (continuation.isActive) {
            continuation.resume(data?.let(::UIImage))
        }
    }
    continuation.invokeOnCancellation { task.cancel() }
    task.resume()
}

private fun String.atsSafeUrl(): String =
    if (startsWith("http://")) "https://${removePrefix("http://")}" else this
