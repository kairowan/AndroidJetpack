package com.ghn.feature.capture.ui.activity

import NetworkCapture
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghn.feature.capture.R
import com.ghn.feature.capture.databinding.ActivityNetworkCaptureBinding
import com.ghn.feature.capture.ui.adapter.NetworkLogAdapter
import com.ghn.feature.capture.utils.binding
import com.ghn.feature.capture.utils.fly
import com.ghn.feature.capture.utils.nullOrThis
import com.ghn.lib.base.aop.TraceTime
import com.ghn.lib.base.aop.confirm.ConfirmAction
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.aop.page.PageAccessGuard
import com.ghn.routermodule.aop.page.PageAccessGuardSupport
import com.ghn.routermodule.feature.FeatureKeys
import com.therouter.router.Route

/**
 * Author: zpj
 * Date: 2023-09-05 15:39
 * Desc: 网络抓包页
 */
@Route(path = RouterPath.Net.NETWORKCAPTURE)
@PageAccessGuard(
    debugOnly = true,
    debugBlockedMessage = "抓包能力仅限调试环境",
    featureKey = FeatureKeys.NETWORK_CAPTURE,
    featureBlockedMessage = "抓包功能当前已关闭"
)
class NetworkCaptureActivity : AppCompatActivity() {
    private val mBinding by binding(ActivityNetworkCaptureBinding::inflate)
    private lateinit var mAdapter: NetworkLogAdapter
    private var mCurPage = 1    // 当前页
    private var mCanLoadMore = true // 能否加载更多

    // 监听
    private val mObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            if (!mBinding.llyFilter.isVisible) loadData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PageAccessGuardSupport.enforce(this)) {
            return
        }
        setContentView(R.layout.activity_network_capture)
        mBinding.apply {
            tvTitle.setOnClickListener {
                it.visibility = View.GONE
                llyFilter.visibility = View.VISIBLE
            }
            tvFilterCancel.setOnClickListener {
                llyFilter.visibility = View.GONE
                tvTitle.visibility = View.VISIBLE
                etFilterContent.setText("")
                loadData()
            }
            etFilterContent.addTextChangedListener(
                afterTextChanged = {
                    if (it != null) loadData(it.toString())
                }
            )
            ivClear.setOnClickListener {
                confirmClearLogs()
            }
            ivSettings.setOnClickListener { fly<ConfigSettingActivity>() }
            rvContent.apply {
                layoutManager = LinearLayoutManager(this@NetworkCaptureActivity)
                mAdapter = NetworkLogAdapter().apply {
                    setOnItemClick {
                        NetworkCapture.currentNetworkLog = it
                        fly<NetworkLogActivity>()
                    }
                }
                adapter = mAdapter
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        (layoutManager as LinearLayoutManager).let {
                            if (it.findLastVisibleItemPosition() == it.itemCount - 1) {
                                if (mCanLoadMore) {
                                    postDelayed({
                                        loadData(etFilterContent.text.toString().nullOrThis(), false)
                                    }, 100L)
                                }
                            }
                        }
                    }
                })
            }
            contentResolver.registerContentObserver(NetworkCapture.networkLogTableUri, true, mObserver)
        }
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(mObserver)
    }

    @ConfirmAction(
        title = "清空抓包记录",
        message = "确定清空当前所有抓包记录吗？"
    )
    private fun confirmClearLogs() {
        NetworkCapture.clearNetworkLog()
        loadData()
    }

    /**
     * 加载数据的方法
     * */
    @TraceTime("network_capture_load_data", warnAtMillis = 24L)
    private fun loadData(filter: String? = null, isRefresh: Boolean = true) {
        if (isRefresh) {
            mCurPage = 0
            mCanLoadMore = true
        } else {
            ++mCurPage
        }
        val data = if (filter.isNullOrBlank())
            NetworkCapture.queryNetworkLog(mCurPage) else NetworkCapture.queryNetworkLogByFilter(filter, mCurPage)
        if (data.isEmpty()) mCanLoadMore = false
        runOnUiThread {
            mAdapter.updateData(isRefresh, data)
            mBinding.rvContent.scrollToPosition(0)
        }
    }

}
