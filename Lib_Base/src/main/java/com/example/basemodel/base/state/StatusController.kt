package com.example.basemodel.base.state

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout


/**
 * @author 浩楠
 * @date 2025/6/17
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO
 */
class StatusController private constructor(
    private val delegate: StatusViewDelegate
) {
    companion object {
        fun bind(
            container: FrameLayout,
            contentView: View,
            init: StatusController.() -> Unit = {}
        ): StatusController {
            val delegate = StatusViewDelegate(container, contentView)
            return StatusController(delegate).apply(init)
        }
    }

    fun setLoadingView(view: View): StatusController {
        delegate.setLoadingView(view)
        return this
    }

    fun setEmptyView(view: View): StatusController {
        delegate.setEmptyView(view)
        return this
    }

    fun setErrorView(view: View): StatusController {
        delegate.setErrorView(view)
        return this
    }

    fun showLoading() = delegate.show(StatusViewState.LOADING)
    fun showEmpty() = delegate.show(StatusViewState.EMPTY)
    fun showError(message: String? = null) = delegate.show(StatusViewState.ERROR, message)
    fun showContent() = delegate.show(StatusViewState.CONTENT)
    fun setRetryAction(action: () -> Unit) = delegate.setRetry(action)
}

