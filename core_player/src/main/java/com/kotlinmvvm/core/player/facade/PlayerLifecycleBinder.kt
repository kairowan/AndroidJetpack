package com.kotlinmvvm.core.player.facade

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kotlinmvvm.core.player.api.IPlayer
import com.kotlinmvvm.core.player.api.PlayState

/**
 * 生命周期与播放器绑定句柄。
 */
class PlayerLifecycleBinding internal constructor(
    private val lifecycle: Lifecycle,
    private val observer: LifecycleEventObserver
) {
    fun unbind() {
        lifecycle.removeObserver(observer)
    }
}

/**
 * 非 Compose 场景的生命周期绑定器。
 */
object PlayerLifecycleBinder {

    @JvmStatic
    fun bind(
        lifecycle: Lifecycle,
        player: IPlayer,
        releaseOnDestroy: Boolean = true
    ): PlayerLifecycleBinding {
        var resumeOnForeground = false
        var released = false

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    resumeOnForeground = player.state.value.isPlaying
                    if (resumeOnForeground) {
                        player.pause()
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    if (resumeOnForeground && player.state.value.playState == PlayState.Ready) {
                        player.resume()
                    }
                    resumeOnForeground = false
                }

                Lifecycle.Event.ON_DESTROY -> {
                    if (releaseOnDestroy && !released) {
                        released = true
                        player.release()
                    }
                }

                else -> Unit
            }
        }

        lifecycle.addObserver(observer)
        return PlayerLifecycleBinding(lifecycle = lifecycle, observer = observer)
    }
}
