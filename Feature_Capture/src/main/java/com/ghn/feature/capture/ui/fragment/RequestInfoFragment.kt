package com.ghn.feature.capture.ui.fragment

import NetworkCapture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghn.feature.capture.R
import com.ghn.feature.capture.databinding.FragmentRequestInfoBinding
import com.ghn.feature.capture.utils.GsonHelper
import com.ghn.feature.capture.utils.HttpHeader
import com.ghn.feature.capture.utils.binding
import com.ghn.feature.capture.utils.generateHeaderView
import com.ghn.feature.capture.utils.isFoldRequestHeaders
import com.ghn.feature.capture.utils.noDataOrThis

/**
 * Author: zpj
 * Date: 2023-09-05 15:41
 * Desc: 请求信息展示页
 */
class RequestInfoFragment : Fragment() {
    companion object {
        fun newInstance() = RequestInfoFragment()
    }

    private val mBinding by binding(FragmentRequestInfoBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_request_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NetworkCapture.currentNetworkLog?.let {
            mBinding?.apply {
                tvRequestUrl.text = it.url
                tvRequestMethod.text = it.method
                llyRequestHeaders.apply {
                    if (it.requestHeaders.isNullOrBlank()) {
                        tvRequestHeaderEmpty.visibility = View.VISIBLE
                    } else {
                        it.requestHeaders!!.let { headers ->
                            GsonHelper.fromJsonArray(headers, HttpHeader::class.java)
                                .forEach { header -> addView(header.generateHeaderView(requireActivity())) }
                        }
                    }
                }
                switchRequestHeadersUI(requireActivity().isFoldRequestHeaders)
                tvRequestHeadersLabel.setOnClickListener {
                    switchRequestHeadersUI(llyRequestHeadersContainer.visibility == View.VISIBLE)
                }
                tvRequestBody.text = it.requestBody.noDataOrThis()
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
     * 切换请求头折叠/展开状态
     * */
    private fun switchRequestHeadersUI(isFold: Boolean = false) {
        mBinding?.apply {
            if (isFold) {
                llyRequestHeadersContainer.visibility = View.GONE
                tvRequestHeadersLabel.text = getText(R.string.cp_network_capture_expand_request_headers)
            } else {
                llyRequestHeadersContainer.visibility = View.VISIBLE
                tvRequestHeadersLabel.text = getText(R.string.cp_network_capture_fold_request_headers)
            }
        }
    }

}
