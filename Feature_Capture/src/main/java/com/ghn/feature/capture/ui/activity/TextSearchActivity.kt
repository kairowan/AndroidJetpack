package com.ghn.feature.capture.ui.activity

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.ghn.feature.capture.R
import com.ghn.feature.capture.databinding.ActivityTextSearchBinding
import com.ghn.feature.capture.utils.LogHelper.TAG
import com.ghn.feature.capture.utils.binding
import com.ghn.feature.capture.utils.getColorRes
import com.ghn.feature.capture.utils.longToast

import java.util.regex.PatternSyntaxException

/**
 * Author: zpj
 * Date: 2023-09-05 17:08
 * Desc: 文本查找页
 */
class TextSearchActivity : AppCompatActivity() {
    private val mBinding by binding(ActivityTextSearchBinding::inflate)
    private var mOriginText: String? = null // 要查找的原始文本
    private var mLineRangeList: MutableList<Pair<Int, Int>> = arrayListOf()  // 存储每行文本的起始和结束下标
    private var mLineHeight = -1 // 每行文本的高度，滑动到特定位置用到
    private var mMatchRangeList: MutableList<Pair<Int, Int>> = arrayListOf()  // 匹配的文本的起始和结束下标
    private var mCurMatchPos = -1   // 当前匹配项下标
    private val mOnClickListener = View.OnClickListener {
        mBinding.apply {
            when (it) {
                ivEnableMatchCase -> {
                    ivEnableMatchCase.isSelected = !ivEnableMatchCase.isSelected
                    searchMatchText()
                }
                ivEnableRegex -> {
                    ivEnableRegex.isSelected = !ivEnableRegex.isSelected
                    searchMatchText()
                }
                tvNextMatch -> {
                    if (!mMatchRangeList.isNullOrEmpty()) {
                        if (mCurMatchPos >= mMatchRangeList.size - 1) mCurMatchPos = 0 else mCurMatchPos++
                        scrollToTargetLine()
                    }
                }
                tvPreviousMatch -> {
                    if (!mMatchRangeList.isNullOrEmpty()) {
                        if (mCurMatchPos <= 0) mCurMatchPos = mMatchRangeList.size - 1 else mCurMatchPos--
                        scrollToTargetLine()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_search)
        mOriginText = NetworkCapture.currentSearchText
        if (!mOriginText.isNullOrBlank()) {
            mLineRangeList = generateLineRangeList()
            mBinding.apply {
                tvSearchContent.text = mOriginText
                // 获取每行文本的高度
                tvSearchContent.post { mLineHeight = tvSearchContent.height / mLineRangeList.size }
                ivEnableMatchCase.isSelected = false
                ivEnableRegex.isSelected = false
                ivEnableMatchCase.setOnClickListener(mOnClickListener)
                ivEnableRegex.setOnClickListener(mOnClickListener)
                tvNextMatch.setOnClickListener(mOnClickListener)
                tvPreviousMatch.setOnClickListener(mOnClickListener)
                etFilterContent.addTextChangedListener(afterTextChanged = { searchMatchText() })
            }
        } else {
            longToast("要查找的文本不能为空！！！")
            finish()
        }
    }

    /**
     * 查找匹配文本
     * */
    private fun searchMatchText() {
        mMatchRangeList.clear()
        mBinding.apply {
            val matchText = etFilterContent.text.toString()
            // 查找文本为空重置文本状态
            if (matchText.isBlank()) {
                tvSearchContent.text = mOriginText
                mBinding.tvMatchCount.text = "未找到匹配项"
            } else {
                // 判断是否为正则匹配，不是indexOf查找，否则正则查找
                if (!ivEnableRegex.isSelected) {
                    var index = mOriginText!!.indexOf(matchText, ignoreCase = !ivEnableMatchCase.isSelected)
                    while (index != -1) {
                        mMatchRangeList.add(index to index + matchText.length)
                        index = mOriginText!!.indexOf(matchText, index + 1, !mBinding.ivEnableMatchCase.isSelected)
                    }
                } else {
                    try {
                        mMatchRangeList = matchText.toRegex().findAll(mOriginText!!).map { result ->
                            result.range.first to result.range.last + 1
                        }.toMutableList()
                    } catch (e: PatternSyntaxException) {
                        Log.d(TAG, "错误的正则表达式")
                    }
                }
                // 如果匹配结果列表不为空
                if (mMatchRangeList.isNotEmpty()) {
                    tvSearchContent.text = SpannableString(mOriginText).apply {
                        mMatchRangeList.forEach { index ->
                            setSpan(
                                BackgroundColorSpan(getColorRes(R.color.match_text)),
                                index.first, index.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    mCurMatchPos = 0 // 默认选中第一个匹配项
                    scrollToTargetLine()
                } else {
                    mBinding.tvSearchContent.text = mOriginText
                    mBinding.tvMatchCount.text = "未找到匹配项"
                }
            }
        }
    }

    /**
     * 生成每行文本
     * */
    private fun generateLineRangeList() = arrayListOf<Pair<Int, Int>>().apply {
        var start = 0
        mOriginText!!.split("\n").forEach {
            val end = start + it.length
            add(start to end)
            start = end + 1
        }
    }

    /**
     * 滚动到目标行
     *
     * 原理：遍历判断匹配项起始和结束坐标，在哪里区间内，对应的下标即为第几行，然后*高度，得出滚动距离
     * */
    private fun scrollToTargetLine() {
        mMatchRangeList[mCurMatchPos].let { range ->
            mBinding.nsvContent.smoothScrollTo(
                0,
                mLineRangeList.indexOfFirst { it.first < range.first && it.second > range.second } * mLineHeight)
            mBinding.tvMatchCount.text = "匹配项：${mCurMatchPos + 1}/${mMatchRangeList.size}"
        }
    }

}