package com.yalantis.ucrop.util

import android.graphics.RectF
import kotlin.math.pow
import kotlin.math.sqrt

object RectUtils {
    /**
     * Gets a float array of the 2D coordinates representing a rectangles
     * corners.
     * The order of the corners in the float array is:
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     *
     * @param r the rectangle to get the corners of
     * @return the float array of corners (8 floats)
     */
    @JvmStatic
    fun getCornersFromRect(r: RectF): FloatArray =
        floatArrayOf(
            r.left,
            r.top,
            r.right,
            r.top,
            r.right,
            r.bottom,
            r.left,
            r.bottom,
        )

    /**
     * Gets a float array of two lengths representing a rectangles width and height
     * The order of the corners in the input float array is:
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     *
     * @param corners the float array of corners (8 floats)
     * @return the float array of width and height (2 floats)
     */
    fun getRectSidesFromCorners(corners: FloatArray): FloatArray =
        floatArrayOf(
            sqrt(
                (corners[0] - corners[2]).toDouble().pow(2.0) + (corners[1] - corners[3]).toDouble().pow(2.0),
            ).toFloat(),
            sqrt(
                (corners[2] - corners[4]).toDouble().pow(2.0) + (corners[3] - corners[5]).toDouble().pow(2.0),
            ).toFloat(),
        )

    @JvmStatic
    fun getCenterFromRect(r: RectF): FloatArray = floatArrayOf(r.centerX(), r.centerY())

    /**
     * Takes an array of 2D coordinates representing corners and returns the
     * smallest rectangle containing those coordinates.
     *
     * @param array array of 2D coordinates
     * @return smallest rectangle containing coordinates
     */
    fun trapToRect(array: FloatArray): RectF {
        val r =
            RectF(
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
            )
        var i = 1
        while (i < array.size) {
            val x = Math.round(array[i - 1] * 10) / 10f
            val y = Math.round(array[i] * 10) / 10f
            r.left = if (x < r.left) x else r.left
            r.top = if (y < r.top) y else r.top
            r.right = if (x > r.right) x else r.right
            r.bottom = if (y > r.bottom) y else r.bottom
            i += 2
        }
        r.sort()
        return r
    }
}
