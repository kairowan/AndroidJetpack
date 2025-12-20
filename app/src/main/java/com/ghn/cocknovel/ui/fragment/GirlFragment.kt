package com.ghn.cocknovel.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.basemodel.base.basefra.BaseFragment
import com.ghn.cocknovel.BR
import com.ghn.cocknovel.R
import com.ghn.cocknovel.databinding.FragmentBookstoreBinding
import com.ghn.cocknovel.databinding.FragmentGirlBinding
import com.ghn.cocknovel.viewmodel.BookStoreViewModel
import com.ghn.cocknovel.viewmodel.GlobalEvent
import com.ghn.eventmodule.EventChannel
import com.ghn.eventmodule.EventChannel.observeEvent
import com.ghn.eventmodule.collectIn
import com.ghn.routermodule.AppRouter
import kotlinx.coroutines.flow.merge
import java.util.Date

data class DemoLoginEvent(val log: String)
class GirlFragment : BaseFragment<FragmentGirlBinding, BookStoreViewModel>(), View.OnClickListener {
//    override fun initVariableId(): Int = BR.mode

    override fun initContentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGirlBinding  = FragmentGirlBinding.inflate(inflater,container,false)


    override fun initParam() {

    }

    override fun initView() {
        mBinding.btn1.setOnClickListener(this)
        mBinding.btn2.setOnClickListener(this)
        mBinding.btn3.setOnClickListener(this)
        mBinding.btn4.setOnClickListener(this)
        mBinding.btn5.setOnClickListener(this)
        mBinding.btn6.setOnClickListener(this)
        mBinding.btn7.setOnClickListener(this)
    }

    override fun initData() {

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn1 ->{
                EventChannel.post(DemoLoginEvent("发送一个数据"))
            }
            R.id.btn2 ->{
                EventChannel.observe<DemoLoginEvent>(sticky = true)
                    .collectIn(viewLifecycleOwner) { event ->
                        showMsg("登录成功：${event.log}")
                    }
            }
            R.id.btn3 ->{
                EventChannel.observeOnlySticky<DemoLoginEvent>()
                    .collectIn(viewLifecycleOwner) { event ->
                        Log.d("历史登录事件", event.toString())
                    }
            }
            R.id.btn4 ->{
                EventChannel.setMaxStickyCacheSize<DemoLoginEvent>(20)
            }
            R.id.btn5 ->{
                val events = EventChannel.getStickyEvents<DemoLoginEvent>()
                events.forEach { (event, time) ->
                    Log.d("events","事件内容=${event}, 发送时间=${Date(time)}")
                }
            }
            R.id.btn6 ->{
                EventChannel.clearStickyEvents<DemoLoginEvent>()
                EventChannel.clearAllStickyEvents()
            }
            R.id.btn7 ->{
                EventChannel.observe<DemoLoginEvent>(sticky = true)
                    .collectIn(viewLifecycleOwner) {
                        Log.i("自定义扩展", "扩展：${it.log}")
                    }

                //或者这么写
//                merge(
//                    EventChannel.observe<GlobalEvent>(sticky = true),
//                    EventChannel.observe<DemoLoginEvent>(sticky = true)
//                ).collectIn(viewLifecycleOwner) { event ->
//                    when (event) {
//                        is GlobalEvent -> handleGlobalEvent(event)
//                        is DemoLoginEvent -> Log.i("扩展", "扩展：${event.log}")
//                    }
//                }
            }
            R.id.btn8 ->{
                observeEvent<DemoLoginEvent> { event ->
                    Log.d("fragment 自动绑定生命周期扩展函数", "事件: ${event}")
                }
            }
            R.id.btn9 ->{
                EventChannel.post(DemoLoginEvent("activity扩展event函数"))
                AppRouter.geToKey()
            }
        }
    }
//    private fun handleGlobalEvent(event: GlobalEvent) {
//        when (event) {
//            is GlobalEvent.TokenExpired -> {
//                // 做跳转处理
//            }
//            is GlobalEvent.ForceLogout -> {
//                // 强制退出登录逻辑
//            }
//            is GlobalEvent.AppLanguageChanged -> {
//                // 刷新语言
//            }
//        }
//    }
}