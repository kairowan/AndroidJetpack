package com.kairowan.lib_ui_common.dialog

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.kairowan.lib_ui_common.R
import com.wang.avi.AVLoadingIndicatorView


/**
 * @author 浩楠
 *
 * @date 2023/1/18   13:48.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述:加载中Dialog
 *  加载的样式
 *  "BallPulseIndicator"
 *  "BallGridPulseIndicator"
 *  "BallClipRotateIndicator"
 *  "BallClipRotatePulseIndicator"
 *  "SquareSpinIndicator"
 *  "BallClipRotateMultipleIndicator"
 *  "BallPulseRiseIndicator"
 *  "BallRotateIndicator"
 *  "CubeTransitionIndicator"
 *  "BallZigZagIndicator"
 *  "BallZigZagDeflectIndicator"
 *  "BallTrianglePathIndicator"
 *  "BallScaleIndicator"
 *  "LineScaleIndicator"
 *  "LineScalePartyIndicator"
 *  "BallScaleMultipleIndicator"
 *  "BallPulseSyncIndicator"
 *  "BallBeatIndicator"
 *  "LineScalePulseOutIndicator"
 *  "LineScalePulseOutRapidIndicator"
 *  "BallScaleRippleIndicator"
 *  "BallScaleRippleMultipleIndicator"
 *  "BallSpinFadeLoaderIndicator"
 *  "LineSpinFadeLoaderIndicator"
 *  "TriangleSkewSpinIndicator"
 *  "PacmanIndicator"
 *  "BallGridBeatIndicator"
 *  "SemiCircleSpinIndicator"
 *  "com.wang.avi.sample.MyCustomIndicator"
 */
class LoadingDialog(context: Context?, themeResId: Int) :
    AlertDialog(context, themeResId) {
    private var avi: AVLoadingIndicatorView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.dialog_loading)
        avi = findViewById<View>(R.id.avi) as AVLoadingIndicatorView
        avi!!.setIndicator("PacmanIndicator")
    }

    override fun show() {
        super.show()
        avi!!.show()
    }

    override fun dismiss() {
        super.dismiss()
        avi!!.hide()
    }

    companion object {
        private var loadingDialog: LoadingDialog? = null
        fun getInstance(context: Context?): LoadingDialog? {
            loadingDialog = LoadingDialog(context, R.style.TransparentDialog) //设置AlertDialog背景透明
            loadingDialog!!.setCancelable(false)
            loadingDialog!!.setCanceledOnTouchOutside(false)
            return loadingDialog
        }
    }
}