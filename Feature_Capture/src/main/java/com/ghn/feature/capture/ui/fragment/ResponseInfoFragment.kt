package com.ghn.feature.capture.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghn.feature.capture.R
import com.ghn.feature.capture.databinding.FragmentRequestInfoBinding
import com.ghn.feature.capture.databinding.FragmentResponseInfoBinding
import com.ghn.feature.capture.utils.GsonHelper
import com.ghn.feature.capture.utils.HttpHeader
import com.ghn.feature.capture.utils.binding
import com.ghn.feature.capture.utils.generateHeaderView
import com.ghn.feature.capture.utils.getColorRes
import com.ghn.feature.capture.utils.getResponseCodeColor
import com.ghn.feature.capture.utils.isFoldRequestHeaders
import com.ghn.feature.capture.utils.noDataOrThis


/**
 * Author: zpj
 * Date: 2023-09-05 15:42
 * Desc: 响应信息展示页
 */
class ResponseInfoFragment : Fragment() {
    companion object {
        fun newInstance() = ResponseInfoFragment()
    }

    private val mBinding by binding(FragmentResponseInfoBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_response_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NetworkCapture.currentNetworkLog?.let {
            mBinding?.apply {
                tvResponseCode.apply {
                    text = "${it.responseCode}"
                    setTextColor(view.context.getColorRes(it.responseCode.getResponseCodeColor()))
                }
                tvResponseTime.text = it.getResponseTimeStr()
                llyResponseHeaders.apply {
                    if (it.responseHeaders.isNullOrBlank()) {
                        tvResponseHeaderEmpty.visibility = View.VISIBLE
                    } else {
                        it.responseHeaders!!.let { headers ->
                            GsonHelper.fromJsonArray(headers, HttpHeader::class.java)
                                .forEach { header -> addView(header.generateHeaderView(requireActivity())) }
                        }
                    }
                }
                switchResponseHeadersUI(requireActivity().isFoldRequestHeaders)
                tvResponseHeadersLabel.setOnClickListener {
                    switchResponseHeadersUI(llyResponseHeadersContainer.visibility == View.VISIBLE)
                }
                tvResponseBody.text = if (it.responseCode != 0) it.responseBody.noDataOrThis() else it.errorMsg
                fbtToTop.setOnClickListener { nsvContent.smoothScrollTo(nsvContent.x.toInt(), 0) }
                nsvContent.viewTreeObserver.addOnScrollChangedListener {
                    if (nsvContent.scrollY == 0 && fbtToTop.visibility == View.VISIBLE) {
                        fbtToTop.hide()
                    } else if (nsvContent.scrollY > 0 && fbtToTop.visibility == View.GONE) {
                        fbtToTop.show()
                    }
                }
            }
        }
    }

    /**
     * 切换响应头折叠/展开状态
     * */
    private fun switchResponseHeadersUI(isFold: Boolean = false) {
        mBinding?.apply {
            if (isFold) {
                llyResponseHeadersContainer.visibility = View.GONE
                tvResponseHeadersLabel.text = getText(R.string.cp_network_capture_expand_response_headers)
            } else {
                llyResponseHeadersContainer.visibility = View.VISIBLE
                tvResponseHeadersLabel.text = getText(R.string.cp_network_capture_fold_response_headers)
            }
        }
    }

}