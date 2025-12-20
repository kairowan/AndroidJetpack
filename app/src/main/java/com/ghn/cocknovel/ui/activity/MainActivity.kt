package com.ghn.cocknovel.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.basemodel.base.baseact.BaseActivity
import com.example.basemodel.base.basevm.BaseViewModel
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.ActivityMainBinding
import com.ghn.cocknovel.ui.fragment.BookshelfFragment
import com.ghn.cocknovel.ui.fragment.BookstoreFragment
import com.ghn.cocknovel.ui.fragment.ClassificationFragment
import com.ghn.cocknovel.ui.fragment.MineFragment
import com.ghn.cocknovel.utils.DebugEntryHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions


class MainActivity : BaseActivity<ActivityMainBinding, BaseViewModel>() {
//    override fun initVariableId(): Int {
//        return BR.mode
//    }

    override fun initContentView(savedInstanceState: Bundle?): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)


    override fun initParam() {

    }

    override fun initView() {
        Log.i("initView", "1111111")
        DebugEntryHelper.attachToActivity(this)
        mBinding.navView.post {
            val navController = findNavController(R.id.nav_host_fragment)
            NavigationUI.setupWithNavController(mBinding.navView, navController)
        }
    }

    override fun initViewObservable() {

    }

    override fun initData() {
        XXPermissions.with(this).permission(Permission.CAMERA)
            .permission(Permission.READ_MEDIA_IMAGES)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        showMsg("获取部分权限成功，但部分权限未正常授予")
                        return
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        showMsg("被永久拒绝授权，请手动授权")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    } else {
                        showMsg("获取权限失败")
                    }
                }
            })
        showMsgWithImage("提示", com.example.basemodel.R.mipmap.ic_my_handes)
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


