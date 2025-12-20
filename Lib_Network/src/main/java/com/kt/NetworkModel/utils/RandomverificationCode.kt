package com.kt.network.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.*

/**
 * @author 浩楠
 *
 * @date 2023/4/11-14:43.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
class RandomverificationCode {
    //settings decided by the layout xml
    //canvas width and height
    private val width = DEFAULT_WIDTH
    private val height = DEFAULT_HEIGHT

    //random word space and pading_top
    private val base_padding_left = BASE_PADDING_LEFT
    private val range_padding_left = RANGE_PADDING_LEFT
    private val base_padding_top = BASE_PADDING_TOP
    private val range_padding_top = RANGE_PADDING_TOP

    //number of chars, lines; font size
    private val codeLength = DEFAULT_CODE_LENGTH
    private val line_number = DEFAULT_LINE_NUMBER
    private val font_size = DEFAULT_FONT_SIZE

    //variables
    private var code: String? = null
    private var padding_left = 0
    private var padding_top = 0
    private val random: Random = Random()

    //验证码图片
    fun createBitmap(): Bitmap {
        padding_left = 0
        val bp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bp)
        code = createCode()
        c.drawColor(Color.WHITE)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textSize = font_size.toFloat()
        //画验证码
        for (i in 0 until code!!.length) {
            randomTextStyle(paint)
            randomPadding()
            c.drawText(code!![i].toString() + "", padding_left.toFloat(),
                padding_top.toFloat(), paint)
        }
//        //画线条
        for (i in 0..line_number) {
            drawLine(c, paint)
        }
        c.save() //保存
        c.restore() //
        return bp
    }

    fun getCode(): String? {
        return code
    }

    //生成验证码
    private fun createCode(): String {
        val buffer = StringBuilder()
        for (i in 0 until codeLength) {
            buffer.append(CHARS[random.nextInt(CHARS.size)])
        }
        return buffer.toString()
    }

    //画干扰线
    private fun drawLine(canvas: Canvas, paint: Paint) {
        val color = randomColor()
        val startX: Int = random.nextInt(width)
        val startY: Int = random.nextInt(height)
        val stopX: Int = random.nextInt(width)
        val stopY: Int = random.nextInt(height)
        paint.strokeWidth = 1F
        paint.color = color
        canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), paint)
    }

    //生成随机颜色
    private fun randomColor(rate: Int = 1): Int {
        val red: Int = random.nextInt(256) / rate
        val green: Int = random.nextInt(256) / rate
        val blue: Int = random.nextInt(256) / rate
        return Color.rgb(red, green, blue)
    }

    //随机生成文字样式，颜色，粗细，倾斜度
    private fun randomTextStyle(paint: Paint) {
        val color = randomColor()
        paint.color = color
        paint.isFakeBoldText = random.nextBoolean() //true为粗体，false为非粗体
        var skewX: Int = random.nextInt(11) / 10
        skewX = if (random.nextBoolean()) skewX else -skewX
        paint.textSkewX = skewX.toFloat() //float类型参数，负数表示右斜，整数左斜
//        paint.isUnderlineText = true; //true为下划线，false为非下划线
//        paint.isStrikeThruText = true; //true为删除线，false为非删除线
    }

    //随机生成padding值
    private fun randomPadding() {
        padding_left += base_padding_left + random.nextInt(range_padding_left)
        padding_top = base_padding_top + random.nextInt(range_padding_top)
    }

    companion object {
        //随机数数组
        private val CHARS = charArrayOf(
            '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm',
            'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
        )
        private var bmpCode: RandomverificationCode? = null
        val instance: RandomverificationCode?
            get() {
                if (bmpCode == null) bmpCode = RandomverificationCode()
                return bmpCode
            }

        //default settings
        //验证码默认随机数的个数
        private const val DEFAULT_CODE_LENGTH = 4

        //默认字体大小
        private const val DEFAULT_FONT_SIZE = 27

        //默认线条的条数
        private const val DEFAULT_LINE_NUMBER = 10

        //padding值
        private const val BASE_PADDING_LEFT = 10
        private const val RANGE_PADDING_LEFT = 15
        private const val BASE_PADDING_TOP = 15
        private const val RANGE_PADDING_TOP = 20

        //验证码的默认宽高
        private const val DEFAULT_WIDTH = 100
        private const val DEFAULT_HEIGHT = 40
    }
}

