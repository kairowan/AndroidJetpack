package com.ghn.module_login

import android.os.Bundle
import com.example.basemodel.base.baseact.BaseActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.module_login.databinding.ActivityLoginBinding
import com.ghn.lib.base.aop.confirm.ConfirmAction
import com.ghn.routermodule.RouterPath
import com.ghn.routermodule.RouterParams
import com.ghn.routermodule.auth.LoginCompletionPage
import com.ghn.routermodule.auth.LoginRequestSessionOwner
import com.ghn.routermodule.auth.LoginRequiredActionCenter
import com.ghn.routermodule.auth.LoginSession
import com.ghn.routermodule.auth.MarkLoginSuccess
import com.therouter.router.Autowired
import com.therouter.router.Route

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
@LoginCompletionPage
class LoginAc: BaseActivity<ActivityLoginBinding, BaseViewModel>(), LoginRequestSessionOwner {

    @Autowired(name = RouterParams.KEY_LOGIN_REQUEST_ID)
    var pendingLoginRequestId: String? = null

    override val loginRequestSessionId: String?
        get() = pendingLoginRequestId

    override fun initContentView(savedInstanceState: Bundle?): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    override fun initParam() {
    }

    override fun initView() {
        mBinding.tvLoginAction.setOnClickListener {
            completeLogin()
        }
        mBinding.tvLogoutAction.setOnClickListener {
            confirmLogout()
        }
    }

    override fun initViewObservable() {
    }

    override fun initData() {

    }

    @ConfirmAction(
        title = "退出登录",
        message = "确定清除当前登录态吗？"
    )
    private fun confirmLogout() {
        LoginRequiredActionCenter.clearPendingAction(pendingLoginRequestId)
        LoginSession.clear()
        showMsg("登录态已清除")
    }

    @MarkLoginSuccess
    private fun completeLogin() {
        LoginSession.updateToken("debug-token-${System.currentTimeMillis()}")
        showMsg("测试登录成功")
        finish()
    }
}
