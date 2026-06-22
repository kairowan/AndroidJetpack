package com.example.basemodel.base.basevm

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel

open class BaseViewModel(application: Application) :
    AndroidViewModel(application),
    BaseViewModelScope,
    BaseViewModelNavigation,
    BaseViewModelLifecycle,
    BaseViewModelLiveData,
    BaseViewModelLauncher {

    override val uc: UIChangeLiveData by lazy { UIChangeLiveData() }

    companion object {
        object ParameterField {
            const val CLASS = "CLASS"
            const val CANONICAL_NAME = "CANONICAL_NAME"
            const val BUNDLE = "BUNDLE"
            const val REQUEST = "REQUEST"
            const val REQEUST_DEFAULT = 1
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
}
