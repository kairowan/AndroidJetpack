package com.example.basemodel.base.state

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.ghn.lib.base.R


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
class StatusViewDelegate(
    private val container: FrameLayout,
    private val contentView: View
) {
    private var loadingView: View? = null
    private var emptyView: View? = null
    private var errorView: View? = null

    private var retryAction: (() -> Unit)? = null

    init {
        container.removeAllViews()
        container.addView(contentView)
    }

    fun setLoadingView(view: View) {
        this.loadingView = view
        container.addView(view)
        view.isVisible = false
    }

    fun setEmptyView(view: View) {
        this.emptyView = view
        container.addView(view)
        view.isVisible = false
    }

    fun setErrorView(view: View) {
        this.errorView = view
        container.addView(view)
        view.isVisible = false
        view.findViewById<View?>(R.id.btn_retry)?.setOnClickListener {
            retryAction?.invoke()
        }
    }

    fun setRetry(action: () -> Unit) {
        retryAction = action
    }

    fun show(state: StatusViewState, errorMsg: String? = null) {
        hideAll()
        when (state) {
            StatusViewState.LOADING -> loadingView?.isVisible = true
            StatusViewState.EMPTY -> emptyView?.isVisible = true
            StatusViewState.ERROR -> {
                errorView?.isVisible = true
                errorMsg?.let {
                    errorView?.findViewById<TextView?>(R.id.tv_error_message)?.text = it
                }
            }
            StatusViewState.CONTENT -> contentView.isVisible = true
        }
    }

    private fun hideAll() {
        loadingView?.isVisible = false
        emptyView?.isVisible = false
        errorView?.isVisible = false
        contentView.isVisible = false
    }
}

