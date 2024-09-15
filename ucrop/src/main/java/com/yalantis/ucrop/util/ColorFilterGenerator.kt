package com.yalantis.ucrop.util

import android.graphics.ColorMatrix
import kotlin.math.max
import kotlin.math.min

object ColorFilterGenerator {
    private val DELTA_INDEX = doubleArrayOf(
        0.0, 0.01, 0.02, 0.04, 0.05, 0.06, 0.07, 0.08, 0.1, 0.11,
        0.12, 0.14, 0.15, 0.16, 0.17, 0.18, 0.20, 0.21, 0.22, 0.24,
        0.25, 0.27, 0.28, 0.30, 0.32, 0.34, 0.36, 0.38, 0.40, 0.42,
        0.44, 0.46, 0.48, 0.5, 0.53, 0.56, 0.59, 0.62, 0.65, 0.68,
        0.71, 0.74, 0.77, 0.80, 0.83, 0.86, 0.89, 0.92, 0.95, 0.98,
        1.0, 1.06, 1.12, 1.18, 1.24, 1.30, 1.36, 1.42, 1.48, 1.54,
        1.60, 1.66, 1.72, 1.78, 1.84, 1.90, 1.96, 2.0, 2.12, 2.25,
        2.37, 2.50, 2.62, 2.75, 2.87, 3.0, 3.2, 3.4, 3.6, 3.8,
        4.0, 4.3, 4.7, 4.9, 5.0, 5.5, 6.0, 6.5, 6.8, 7.0,
        7.3, 7.5, 7.8, 8.0, 8.4, 8.7, 9.0, 9.4, 9.6, 9.8,
        10.0
    )

    /**
     * @param cm
     * @param value
     * @see http://groups.google.com/group/android-developers/browse_thread/thread/9e215c83c3819953
     * @see http://gskinner.com/blog/archives/2007/12/colormatrix_cla.html
     */
    fun adjustHue(cm: ColorMatrix, value: Float) {
        var value = value
        value = cleanValue(value, 180f) / 180f * Math.PI.toFloat()
        if (value == 0f) {
            return
        }
        val cosVal = Math.cos(value.toDouble()).toFloat()
        val sinVal = Math.sin(value.toDouble()).toFloat()
        val lumR = 0.213f
        val lumG = 0.715f
        val lumB = 0.072f
        val mat = floatArrayOf(
            lumR + cosVal * (1 - lumR) + sinVal * -lumR,
            lumG + cosVal * -lumG + sinVal * -lumG,
            lumB + cosVal * -lumB + sinVal * (1 - lumB),
            0f,
            0f,
            lumR + cosVal * -lumR + sinVal * 0.143f,
            lumG + cosVal * (1 - lumG) + sinVal * 0.140f,
            lumB + cosVal * -lumB + sinVal * -0.283f,
            0f,
            0f,
            lumR + cosVal * -lumR + sinVal * -(1 - lumR),
            lumG + cosVal * -lumG + sinVal * lumG,
            lumB + cosVal * (1 - lumB) + sinVal * lumB,
            0f,
            0f,
            0f,
            0f,
            0f,
            1f,
            0f,
            0f,
            0f,
            0f,
            0f,
            1f
        )
        cm.postConcat(ColorMatrix(mat))
    }

    @JvmStatic
    fun adjustBrightness(cm: ColorMatrix, value: Float): Float {
        var value = value
        value = cleanValue(value, 100f)
        if (value == 0f) {
            return value
        }
        val mat = floatArrayOf(
            1f, 0f, 0f, 0f, value,
            0f, 1f, 0f, 0f, value,
            0f, 0f, 1f, 0f, value,
            0f, 0f, 0f, 1f, 0f,
            0f, 0f, 0f, 0f, 1f
        )
        cm.postConcat(ColorMatrix(mat))
        return value
    }

    @JvmStatic
    fun adjustContrast(cm: ColorMatrix, value: Float): Float {
        var value = value
        value = cleanValue(value, 50f)
        if (value == 0f) {
            return value
        }
        var x: Float
        if (value < 0) {
            x = 127 + value / 100 * 127
        } else {
            val valueInt = value.toInt()
            x = value % 2
            x = if (x == 0f) {
                DELTA_INDEX[valueInt].toFloat()
            } else {
                DELTA_INDEX[valueInt shl 0].toFloat() * (1 - x) + DELTA_INDEX[(valueInt shl 0) + 1].toFloat() * x // use linear interpolation for more granularity.
            }
            x = x * 127 + 127
        }
        val mat = floatArrayOf(
            x / 127, 0f, 0f, 0f, 0.5f * (127 - x),
            0f, x / 127, 0f, 0f, 0.5f * (127 - x),
            0f, 0f, x / 127, 0f, 0.5f * (127 - x),
            0f, 0f, 0f, 1f, 0f,
            0f, 0f, 0f, 0f, 1f
        )
        cm.postConcat(ColorMatrix(mat))
        return value
    }

    @JvmStatic
    fun adjustSaturation(cm: ColorMatrix, value: Float): Float {
        var value = value
        value = cleanValue(value, 100f)
        if (value == 0f) {
            return value
        }
        val x = 1 + if (value > 0) 3 * value / 100 else value / 100
        val lumR = 0.3086f
        val lumG = 0.6094f
        val lumB = 0.0820f
        val mat = floatArrayOf(
            lumR * (1 - x) + x, lumG * (1 - x), lumB * (1 - x), 0f, 0f,
            lumR * (1 - x), lumG * (1 - x) + x, lumB * (1 - x), 0f, 0f,
            lumR * (1 - x), lumG * (1 - x), lumB * (1 - x) + x, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
            0f, 0f, 0f, 0f, 1f
        )
        cm.postConcat(ColorMatrix(mat))
        return value
    }

    private fun cleanValue(p_val: Float, p_limit: Float): Float {
        return min(p_limit, max(-p_limit, p_val))
    }
}