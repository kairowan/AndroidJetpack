package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import android.util.Log
import com.example.basemodel.base.baseact.BaseActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.example.basemodel.base.state.StatusController
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.databinding.HomeActivityBinding
import com.ghn.routermodule.RouterPath
import com.therouter.router.Route


/**
 * @author 浩楠
 * @date 2025/5/30 18:07
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO
 */
@Route(path = RouterPath.Main.HOME)
class HomeActivity : BaseActivity<HomeActivityBinding, BaseViewModel>() {

    private lateinit var statusController: StatusController

//    override fun initVariableId(): Int = BR.mode

    override fun initContentView(savedInstanceState: Bundle?): HomeActivityBinding =
        HomeActivityBinding.inflate(layoutInflater)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun initParam() {
    }

    override fun initView() {

    }

    override fun initViewObservable() {
    }

    override fun initData() {
    }
}