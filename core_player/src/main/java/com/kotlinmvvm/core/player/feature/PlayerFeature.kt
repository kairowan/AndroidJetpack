package com.kotlinmvvm.core.player.feature

import com.kotlinmvvm.core.player.api.IPlayer

/**
 * 播放器功能扩展点。
 */
interface PlayerFeature {
    fun onAttach(player: IPlayer) {}
    fun onDetach(player: IPlayer) {}
    fun onUrlChanged(player: IPlayer, url: String, autoPlay: Boolean) {}
}
