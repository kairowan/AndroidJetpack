package com.example.basemodel.base.baseint

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * @author 浩楠
 *
 * @date 2023/6/4-16:10
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO 通过接口类来实现我们需要的方法，并且便于后期我们对base的新增和修改
 */
interface IBaseView : DefaultLifecycleObserver {

    /**
     * 初始化界面传递参数，例如获取 intent 传值
     */
    fun initParam()

    /**
     * 初始化界面元素，例如 RecyclerView、按钮等 UI 元素
     */
    fun initView()

    /**
     * 初始化界面观察者的监听，例如 LiveData、Flow 等
     */
    fun initViewObservable()

    /**
     * 初始化数据，例如从网络/本地加载数据
     */
    fun initData()

    override fun onCreate(owner: LifecycleOwner) {
        initParam()
        initView()
        initViewObservable()
        initData()
    }
}