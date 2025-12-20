package com.example.basemodel.base.basevm

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.basemodel.base.SingleLiveEvent


/**
 * @author 浩楠
 * @date 2025/5/29 14:37
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO
 */
class UIChangeLiveData : SingleLiveEvent<Any?>() {
    private var startActivityEvent: SingleLiveEvent<Map<String, Any>>? = null
    private var getStartModelActivityEvent: SingleLiveEvent<Map<String, Any>>? = null
    private var finishEvent: SingleLiveEvent<Void>? = null
    private var onBackPressedEvent: SingleLiveEvent<Void>? = null
    private var setResultEvent: SingleLiveEvent<Map<String, String>>? = null
    private var finishResult: SingleLiveEvent<Int>? = null
    private var startActivityForFragment: SingleLiveEvent<Map<String, Any>>? = null
    private var setResultFragment: SingleLiveEvent<Map<String, Any>>? = null
    private var showDialog: SingleLiveEvent<String>? = null
    private var toastEvent: SingleLiveEvent<String>? = null
    private var dismissDialog: SingleLiveEvent<Void>? = null
    fun getShowDialog(): SingleLiveEvent<String> {
        return createLiveData(showDialog).also {
            showDialog = it
        }
    }

    fun getDismissDialog(): SingleLiveEvent<Void> {
        return createLiveData(dismissDialog).also {
            dismissDialog = it
        }
    }

    fun toastEvent(): SingleLiveEvent<String> {
        return createLiveData(toastEvent).also {
            toastEvent = it
        }
    }

    fun getResultFragment(): SingleLiveEvent<Map<String, Any>> {
        return createLiveData(setResultFragment).also {
            setResultFragment = it
        }
    }

    fun getStartActivityForFragment(): SingleLiveEvent<Map<String, Any>> {
        return createLiveData(startActivityForFragment).also {
            startActivityForFragment = it
        }
    }

    fun getFinishResult(): SingleLiveEvent<Int> {
        return createLiveData(finishResult).also {
            finishResult = it
        }
    }

    fun getStartActivityEvent(): SingleLiveEvent<Map<String, Any>> {
        return createLiveData(startActivityEvent).also {
            startActivityEvent = it
        }

    }

    fun getStartModelActivityEvent(): SingleLiveEvent<Map<String, Any>> {
        Log.i("TAG", "getStartModelActivityEvent: 111")
        return createLiveData(getStartModelActivityEvent).also {
            getStartModelActivityEvent = it
        }

    }

    fun getSetResultEvent(): SingleLiveEvent<Map<String, String>> {
        return createLiveData(setResultEvent).also {
            setResultEvent = it
        }
    }

    fun getFinishEvent(): SingleLiveEvent<Void> {
        return createLiveData(finishEvent).also {
            finishEvent = it
        }
    }

    fun getOnBackPressedEvent(): SingleLiveEvent<Void> {
        return createLiveData(onBackPressedEvent).also {
            onBackPressedEvent = it
        }
    }


    private fun <T> createLiveData(liveData: SingleLiveEvent<T>?): SingleLiveEvent<T> {

        var mLive: SingleLiveEvent<T>? = liveData
        liveData?.let {
            return mLive!!
        } ?: let {
            mLive = SingleLiveEvent()
        }


        return mLive!!
    }


    override fun observe(owner: LifecycleOwner, observer: Observer<in Any?>) {
        super.observe(owner, observer)
    }
}