package com.ghn.feature.capture.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ghn.feature.capture.R
import com.ghn.feature.capture.entity.NetworkLog
import com.ghn.feature.capture.utils.bytesToStr
import com.ghn.feature.capture.utils.getColorRes
import com.ghn.feature.capture.utils.getResponseCodeColor


/**
 * Author: zpj
 * Date: 2023-09-05 15:40
 * Desc: 网络日志Adapter
 */
class NetworkLogAdapter : RecyclerView.Adapter<NetworkLogAdapter.Holder>() {
    private var mData: MutableList<NetworkLog> = mutableListOf()
    private var mItemClick: ((NetworkLog) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_network_log, parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindData(mData[position])
    }

    override fun getItemCount() = mData.size

    fun setOnItemClick(callback: (NetworkLog) -> Unit) {
        this.mItemClick = callback
    }

    // 更新数据的方法
    fun updateData(isRefresh: Boolean = true, data: ArrayList<NetworkLog>) {
        val newList = if (isRefresh) data else (mData + data).toMutableList()
        DiffUtil.calculateDiff(DiffCallback(mData, newList)).dispatchUpdatesTo(this)
        mData = newList
    }

    inner class Holder(private val containerView: View) : RecyclerView.ViewHolder(containerView) {
        fun bindData(data: NetworkLog) {
            containerView.apply {
                setOnClickListener { mItemClick?.invoke(data) }
                findViewById<TextView>(R.id.tv_host).text = "${data.scheme}://${data.host}"
                findViewById<TextView>(R.id.tv_path).text = "${data.path}"
                findViewById<TextView>(R.id.tv_response_code).apply {
                    text = "${data.responseCode}"
                    setTextColor(containerView.context.getColorRes(data.responseCode.getResponseCodeColor()))
                }
                findViewById<TextView>(R.id.tv_method).text = "${data.method}"
                findViewById<TextView>(R.id.tv_request_time).text = "${data.getRequestTimeStr()}"
                findViewById<TextView>(R.id.tv_duration).text = "${data.duration}ms"
                findViewById<TextView>(R.id.tv_response_content_length).text =
                    "${bytesToStr(data.responseContentLength)}"
            }
        }
    }

    inner class DiffCallback(oldList: List<NetworkLog>, newList: List<NetworkLog>) : DiffUtil.Callback() {
        private var mOldList: List<NetworkLog> = oldList
        private var mNewList: List<NetworkLog> = newList

        override fun getOldListSize() = mOldList.size
        override fun getNewListSize() = mNewList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            mOldList[oldItemPosition].id == mNewList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            mOldList[oldItemPosition].id == mNewList[newItemPosition].id
    }

}