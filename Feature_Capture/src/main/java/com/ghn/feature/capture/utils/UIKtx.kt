package com.ghn.feature.capture.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.ghn.feature.capture.R
import com.ghn.feature.capture.utils.LogHelper.TAG
import java.io.Serializable

/**
 * Author: zpj
 * Date: 2023-09-05 15:14
 * Desc: UI相关扩展
 */


/**
 * 获取颜色值
 * */
internal fun Context.getColorRes(resId: Int) = ContextCompat.getColor(this, resId)

/**
 * 获得不同状态码对应的颜色值
 * */
internal fun Int?.getResponseCodeColor() = when (this) {
    in 100..199 -> R.color.response_code_1xx
    in 200..299 -> R.color.response_code_2xx
    in 300..399 -> R.color.response_code_3xx
    in 400..499 -> R.color.response_code_4xx
    else -> R.color.response_code_5xx
}


/* *************************************************************
 *                           尺寸转换相关
 ************************************************************ */
internal val Float.dp: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

internal val Int.dp: Int
    get() = toFloat().dp.toInt()


/* *************************************************************
 *                           Toast提示
 ************************************************************ */
fun Context.shortToast(msg: String) = Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
fun Context.longToast(msg: String) = Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()


/* *************************************************************
 *                           Activity跳转扩展
 ************************************************************ */
inline fun <reified T : Activity> Activity.fly(
    intent: Intent = Intent(this, T::class.java),
    bundle: Bundle? = null,
    isFinish: Boolean = false,
    requestCode: Int? = null,
    vararg params: Pair<String, Any> = emptyArray()
) {
    params.forEach { intent.putExtraEx(it) }
    bundle?.let { intent.putExtras(bundle) }
    try {
        intent.resolveActivity(packageManager)?.also {
            if (requestCode == null) startActivity(intent) else startActivityForResult(intent, requestCode)
            if (isFinish) finish()
        }
    } catch (e: ActivityNotFoundException) {
        shortToast("Activity未找到：${intent}")
    } catch (e: Exception) {
        e.message?.let { Log.e(TAG, it) }
    }
}

fun Intent.putExtraEx(extra: Pair<String, Any>) {
    when (val data = extra.second) {
        is Array<*> -> {
            when {
                data.isArrayOf<Boolean>() -> putExtra(extra.first, data as BooleanArray)
                data.isArrayOf<Byte>() -> putExtra(extra.first, data as ByteArray)
                data.isArrayOf<Short>() -> putExtra(extra.first, data as ShortArray)
                data.isArrayOf<Int>() -> putExtra(extra.first, data as IntArray)
                data.isArrayOf<Long>() -> putExtra(extra.first, data as LongArray)
                data.isArrayOf<Float>() -> putExtra(extra.first, data as FloatArray)
                data.isArrayOf<Double>() -> putExtra(extra.first, data as DoubleArray)
                data.isArrayOf<Char>() -> putExtra(extra.first, data as CharArray)
                data.isArrayOf<Serializable>() -> putExtra(extra.first, data as Array<out Serializable>)
                data.isArrayOf<Parcelable>() -> putExtra(extra.first, data as Array<out Parcelable>)
                data.isArrayOf<CharSequence>() -> putExtra(extra.first, data as Array<out CharSequence>)
                data.isArrayOf<String>() -> putExtra(extra.first, data as Array<out String>)
            }
        }
        is Boolean -> putExtra(extra.first, data)
        is Byte -> putExtra(extra.first, data)
        is Short -> putExtra(extra.first, data)
        is Int -> putExtra(extra.first, data)
        is Long -> putExtra(extra.first, data)
        is Float -> putExtra(extra.first, data)
        is FloatArray -> putExtra(extra.first, data)
        is Double -> putExtra(extra.first, data)
        is DoubleArray -> putExtra(extra.first, data)
        is Char -> putExtra(extra.first, data)
        is CharArray -> putExtra(extra.first, data)
        is CharSequence -> putExtra(extra.first, data)
        is String -> putExtra(extra.first, data)
        is Bundle -> putExtra(extra.first, data)
        is Parcelable -> putExtra(extra.first, data)
        is Serializable -> putExtra(extra.first, data)
    }
}

/* *************************************************************
 *                           ViewBinding扩展
 ************************************************************ */
internal inline fun <VB : ViewBinding> Activity.binding(crossinline inflate: (LayoutInflater) -> VB) = lazy {
    inflate(layoutInflater).apply { setContentView(root) }
}

internal fun <VB : ViewBinding> Fragment.binding(bind: (View) -> VB) = lazy {
    var vb: VB? = null
    view?.let { vb = bind(it) }
    viewLifecycleOwnerLiveData.observe(this) {
        it.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event.targetState == Lifecycle.State.DESTROYED) vb = null
            }
        })
    }
    vb
}