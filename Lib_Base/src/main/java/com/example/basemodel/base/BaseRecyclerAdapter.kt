package com.example.basemodel.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * @author 浩楠
 *
 * @date 2023/4/7-14:03.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
abstract class BaseRecyclerAdapter<T>(protected var datas: MutableList<T>) :
    RecyclerView.Adapter<BaseRecyclerAdapter<T>.BaseViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): BaseViewHolder {
        val from =
            LayoutInflater.from(viewGroup.context)
        val inflate = from.inflate(layoutId, viewGroup, false)
        return BaseViewHolder(inflate)
    }

    override fun onBindViewHolder(
        baseViewHolder: BaseViewHolder,
        position: Int
    ) {
        bindData(baseViewHolder, position)

    }

    /**
     * 刷新数据
     * @param datas
     */
    fun refresh(datas: List<T>?) {
        if (datas != null) {
            this.datas.clear()
            notifyItemRangeRemoved(0, datas.size)
            this.datas.addAll(datas)
            notifyItemRangeInserted(0, datas.size)
        }
    }

    /**
     * 刷新数据
     * @param datas
     */
    fun refreshAll(datas: List<T>?) {
        if (datas != null) {
            this.datas.clear()
            this.datas.addAll(datas)
            notifyDataSetChanged()
        }
    }

    /**
     * 添加数据
     * @param datas
     */
    fun addData(datas: List<T>) {
        this.datas.addAll(datas)
        notifyItemRangeChanged(0, datas.size)
    }

    /**
     * 添加数据
     * @param
     */
    fun addData(data: T) {
        datas.add(data)
        notifyItemRangeChanged(0, datas.size)
    }

    /**
     * 删除数据
     * @param
     */
    fun delete() {
        datas.clear()
        notifyDataSetChanged()
    }

    /**
     * 删除数据
     * @param
     */
    fun delete(p: Int) {
        datas.removeAt(p)
        notifyItemRemoved(p)
        notifyItemChanged(p)
    }

    /**
     * 绑定数据
     * @param holder  具体的viewHolder
     * @param position  对应的索引
     */
    protected abstract fun bindData(holder: BaseViewHolder?, position: Int)
    override fun getItemCount(): Int {
        return datas.size
    }

    /**
     * 封装ViewHolder ,子类可以直接使用
     */
    inner class BaseViewHolder(itemView: View?) :
        RecyclerView.ViewHolder(itemView!!) {
        private val mViewMap: MutableMap<Int, View?>

        /**
         * 获取设置的view
         * @param id
         * @return
         */
        fun getView(id: Int): View? {
            var view = mViewMap[id]
            if (view == null) {
                view = itemView.findViewById(id)
                mViewMap[id] = view
            }
            return view
        }

        init {
            mViewMap = HashMap()
        }
    }

    /**
     * 获取子item
     * @return
     */
    abstract val layoutId: Int

}