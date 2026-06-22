package com.example.basemodel.base.baseint

import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface IBaseViewModel : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {}

    override fun onDestroy(owner: LifecycleOwner) {}

    override fun onStart(owner: LifecycleOwner) {}

    override fun onStop(owner: LifecycleOwner) {}

    override fun onResume(owner: LifecycleOwner) {}

    override fun onPause(owner: LifecycleOwner) {}

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}
