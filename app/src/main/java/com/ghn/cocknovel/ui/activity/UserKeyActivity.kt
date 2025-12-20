package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import com.example.basemodel.base.baseact.BaseActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.databinding.UserLeyActivityBinding
import com.ghn.cocknovel.ui.fragment.DemoLoginEvent
import com.ghn.eventmodule.EventChannel.observeEvent
import com.ghn.routermodule.RouterPath
import com.therouter.router.Route


/**
 * @author 浩楠
 * @date 2025/5/30 18:12
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO
 */
@Route(path = RouterPath.User.UserKEY)
class UserKeyActivity : BaseActivity<UserLeyActivityBinding, BaseViewModel>() {
//    override fun initVariableId(): Int = BR.mode
    override fun initContentView(savedInstanceState: Bundle?): UserLeyActivityBinding =
        UserLeyActivityBinding.inflate(layoutInflater)


    override fun initParam() {
    }

    override fun initView() {
    }

    override fun initViewObservable() {
        observeEvent<DemoLoginEvent>(sticky = true) { event ->
            showMsg("收到事件：${event.toString()}")
        }
    }

    override fun initData() {
    }
}