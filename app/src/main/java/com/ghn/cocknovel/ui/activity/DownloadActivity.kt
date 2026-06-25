package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.basemodel.base.baseact.BaseActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.cocknovel.databinding.ActivityDownloadBinding
import com.ghn.lib.download.DownloadService
import com.ghn.lib.download.DownloadTaskRequest
import com.ghn.lib.base.aop.network.RequireNetwork
import com.ghn.routermodule.aop.guard.PreventRepeat
import com.example.flowdownload.download.model.DownloadSnapshot
import com.example.flowdownload.download.model.DownloadStatus
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DownloadActivity : BaseActivity<ActivityDownloadBinding, BaseViewModel>() {
    private val downloadService: DownloadService by inject()
    private val trackedDownloadNames = linkedMapOf<String, String>()
    private val batchDownloadIds = mutableListOf<String>()
    private var singleDownloadId: String? = null
    private val urls = listOf(
        "https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/apk/sample-debug.apk",
        "https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/json/json1.txt",
        "https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/json/json.txt"
    )

//    override fun initVariableId(): Int = BR._all
    override fun initContentView(savedInstanceState: Bundle?): ActivityDownloadBinding  = ActivityDownloadBinding.inflate(layoutInflater)

    override fun initParam() {
        mBinding.TvDownload.setOnClickListener {
            startSingleDownload()
        }
        mBinding.TvDownloads.setOnClickListener {
            startBatchDownloads()
        }
    }

    override fun initView() {

    }

    override fun initViewObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                downloadService.observeAll().collect(::renderDownloadSnapshots)
            }
        }
    }

    override fun initData() {

    }

    @RequireNetwork(message = "当前无网络，无法开始下载")
    @PreventRepeat(
        intervalMillis = 1500L,
        key = "download_single_file",
        message = "下载任务正在准备，请勿重复点击"
    )
    private fun startSingleDownload() {
        val fileName = "wechat.apk"
        lifecycleScope.launch {
            runCatching {
                downloadService.enqueue(
                    DownloadTaskRequest(
                        url = "https://dldir1.qq.com/weixin/android/weixin8015android2020_arm64.apk",
                        displayName = fileName,
                        subDirectory = "demo",
                        group = SINGLE_GROUP,
                    )
                )
            }.onSuccess { result ->
                singleDownloadId = result.downloadId.value
                trackedDownloadNames[result.downloadId.value] = fileName
                renderDownloadSnapshots(emptyList())
            }.onFailure { throwable ->
                showMsg(throwable.message ?: "下载任务创建失败")
            }
        }
    }

    @RequireNetwork(message = "当前无网络，无法开始批量下载")
    @PreventRepeat(
        intervalMillis = 1500L,
        key = "download_batch_files",
        message = "批量下载任务正在准备，请勿重复点击"
    )
    private fun startBatchDownloads() {
        batchDownloadIds.clear()
        mBinding.TvPercents.text = ""
        lifecycleScope.launch {
            urls.forEach { url ->
                val fileName = url.substringAfterLast('/')
                runCatching {
                    downloadService.enqueue(
                        DownloadTaskRequest(
                            url = url,
                            displayName = fileName,
                            subDirectory = "demo/batch",
                            group = BATCH_GROUP,
                        )
                    )
                }.onSuccess { result ->
                    batchDownloadIds += result.downloadId.value
                    trackedDownloadNames[result.downloadId.value] = fileName
                }.onFailure { throwable ->
                    showMsg(throwable.message ?: "$fileName 下载任务创建失败")
                }
            }
        }
    }

    private fun renderDownloadSnapshots(snapshots: List<DownloadSnapshot>) {
        val snapshotMap = snapshots.associateBy { it.id.value }
        singleDownloadId?.let { id ->
            val fileName = trackedDownloadNames[id] ?: "单文件"
            mBinding.TvPercent.text = snapshotMap[id]?.toDisplayText(fileName).orEmpty()
        }
        if (batchDownloadIds.isEmpty()) {
            return
        }
        mBinding.TvPercents.text = batchDownloadIds.joinToString(separator = "\n") { id ->
            val fileName = trackedDownloadNames[id] ?: id
            snapshotMap[id]?.toDisplayText(fileName) ?: "$fileName: 等待任务启动"
        }
    }
}

private fun DownloadSnapshot.toDisplayText(fileName: String): String {
    return when (val currentStatus = status) {
        DownloadStatus.Queued -> "$fileName: 排队中"
        is DownloadStatus.RetryWaiting -> "$fileName: 等待重试(${retryCount}/${maxRetries})"
        DownloadStatus.Starting -> "$fileName: 准备中"
        is DownloadStatus.Running -> {
            val progress = progressPercent ?: 0
            "$fileName: ${progress}%"
        }

        DownloadStatus.Paused -> "$fileName: 已暂停"
        DownloadStatus.Success -> "$fileName: 下载完成"
        DownloadStatus.Cancelled -> "$fileName: 已取消"
        is DownloadStatus.Failed -> "$fileName: 失败 ${currentStatus.failure.message}"
    }
}

private const val SINGLE_GROUP = "download_demo_single"
private const val BATCH_GROUP = "download_demo_batch"
