package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.basemodel.base.baseact.BaseActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.ActivityMainBinding
import com.ghn.cocknovel.utils.DebugEntryHelper
import com.ghn.lib.base.aop.permission.capability.RequireMediaPermission


class MainActivity : BaseActivity<ActivityMainBinding, BaseViewModel>() {
    override fun initContentView(savedInstanceState: Bundle?): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)


    override fun initParam() {

    }

    override fun initView() {
        DebugEntryHelper.attachToActivity(this)
        mBinding.navView.post {
            val navController = findNavController(R.id.nav_host_fragment)
            NavigationUI.setupWithNavController(mBinding.navView, navController)
        }
    }

    override fun initViewObservable() {

    }

    override fun initData() {
        requestMediaPermission()
    }

    @RequireMediaPermission(
        tag = "main_media_permission"
    )
    private fun requestMediaPermission() {
        showMsgWithImage("媒体权限已获取", com.ghn.lib.base.R.mipmap.ic_my_handes)
    }

    private var exitTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                mBinding.container.let { showMsg("再按一次退出鲸鱼阅读") }
                exitTime = System.currentTimeMillis()
            } else {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


}
