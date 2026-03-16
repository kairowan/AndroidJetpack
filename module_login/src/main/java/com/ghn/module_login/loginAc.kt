package com.ghn.module_login

import android.os.Bundle
import com.example.basemodel.base.baseact.BaseActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.module_login.databinding.ActivityLoginBinding
import com.ghn.routermodule.RouterPath
import com.therouter.router.Route
import com.therouter.router.Routes

/**
 * @author 浩楠
 *
 * @date 2025/12/20
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
@Route(path = RouterPath.Login.LoginAC)
class loginAc: BaseActivity<ActivityLoginBinding, BaseViewModel>() {
    override fun initContentView(savedInstanceState: Bundle?): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    override fun initParam() {
    }

    override fun initView() {
    }

    override fun initViewObservable() {
    }

    override fun initData() {
        initLogin()
    }

}