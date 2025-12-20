package com.ghn.feature.capture.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghn.feature.capture.R
import com.ghn.feature.capture.databinding.ActivityNetworkLogBinding
import com.ghn.feature.capture.ui.adapter.NetworkLogPagerAdapter
import com.ghn.feature.capture.ui.fragment.RequestInfoFragment
import com.ghn.feature.capture.ui.fragment.ResponseInfoFragment
import com.ghn.feature.capture.utils.binding


/**
 * Author: zpj
 * Date: 2023-09-05 15:41
 * Desc: 请求日志页
 */
class NetworkLogActivity : AppCompatActivity() {
    private val mBinding by binding(ActivityNetworkLogBinding::inflate)
    private lateinit var mPageAdapter: NetworkLogPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_log)
        mBinding.apply {
            NetworkCapture.currentNetworkLog?.let {
                mPageAdapter = NetworkLogPagerAdapter(supportFragmentManager).apply {
                    addFragment(RequestInfoFragment.newInstance(), "请求")
                    addFragment(ResponseInfoFragment.newInstance(), "响应")
                }
                vpContent.adapter = mPageAdapter
                tlContent.setupWithViewPager(vpContent)
                // 去掉Tab长按提示文字
                (0 until mPageAdapter.count).forEach { tlContent.getTabAt(it)?.view?.isLongClickable = false }
            }
        }

    }
}