package com.yalantis.ucrop.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.net.Uri
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.exifinterface.media.ExifInterface
import com.yalantis.ucrop.callback.BitmapLoadCallback
import com.yalantis.ucrop.task.BitmapLoadTask
import com.yalantis.ucrop.util.EglUtils.maxTextureSize
import java.io.Closeable
import java.io.IOException
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
object BitmapLoadUtils {
    private const val TAG = "BitmapLoadUtils"
    private const val CONTENT_SCHEME: String = "content"

    @JvmStatic
    fun decodeBitmapInBackground(
        context: Context,
        uri: Uri, outputUri: Uri,
        requiredWidth: Int, requiredHeight: Int,
        loadCallback: BitmapLoadCallback?
    ) {
        BitmapLoadTask(context, uri, outputUri, requiredWidth, requiredHeight, loadCallback!!).execute()
    }

    fun transformBitmap(bitmap: Bitmap, transformMatrix: Matrix): Bitmap {
        var bitmap = bitmap
        try {
            val converted = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                transformMatrix,
                true
            )
            if (!bitmap.sameAs(converted)) {
                bitmap = converted
            }
        } catch (error: OutOfMemoryError) {
            Log.e(TAG, "transformBitmap: ", error)
        }
        return bitmap
    }

    fun decodeDimensions(
        context: Context,
        uri: Uri?,
        options: BitmapFactory.Options
    ) {
        options.inJustDecodeBounds = true
        try {
            val `is` = context.contentResolver.openInputStream(uri!!)
            try {
                BitmapFactory.decodeStream(`is`, null, options)
            } finally {
                close(`is`)
            }
        } catch (ignored: IOException) {
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width lower or equal to the requested height and width.
            while (height / inSampleSize > reqHeight || width / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun getExifOrientation(context: Context, imageUri: Uri): Int {
        var orientation = ExifInterface.ORIENTATION_UNDEFINED
        try {
            val stream = context.contentResolver.openInputStream(imageUri) ?: return orientation
            orientation = ImageHeaderParser(stream).orientation
            close(stream)
        } catch (e: IOException) {
            Log.e(TAG, "getExifOrientation: $imageUri", e)
        }
        return orientation
    }

    fun exifToDegrees(exifOrientation: Int): Int {
        val rotation: Int
        rotation = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90
            ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
            ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270
            else -> 0
        }
        return rotation
    }

    fun exifToTranslation(exifOrientation: Int): Int {
        val translation: Int
        translation = when (exifOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL, ExifInterface.ORIENTATION_FLIP_VERTICAL, ExifInterface.ORIENTATION_TRANSPOSE, ExifInterface.ORIENTATION_TRANSVERSE -> -1
            else -> 1
        }
        return translation
    }

    /**
     * This method calculates maximum size of both width and height of bitmap.
     * It is twice the device screen diagonal for default implementation (extra quality to zoom image).
     * Size cannot exceed max texture size.
     *
     * @return - max bitmap size in pixels.
     */
    @JvmStatic
    fun calculateMaxBitmapSize(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display: Display
        val width: Int
        val height: Int
        val size = Point()
        if (wm != null) {
            display = wm.defaultDisplay
            display.getSize(size)
        }
        width = size.x
        height = size.y

        // Twice the device screen diagonal as default
        var maxBitmapSize =
            sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0)).toInt()

        // Check for max texture size via Canvas
        val canvas = Canvas()
        val maxCanvasSize = min(canvas.maximumBitmapWidth, canvas.maximumBitmapHeight)
        if (maxCanvasSize > 0) {
            maxBitmapSize = min(maxBitmapSize, maxCanvasSize)
        }

        // Check for max texture size via GL
        val maxTextureSize = maxTextureSize
        if (maxTextureSize > 0) {
            maxBitmapSize = min(maxBitmapSize, maxTextureSize)
        }
        Log.d(TAG, "maxBitmapSize: $maxBitmapSize")
        return maxBitmapSize
    }

    @JvmStatic
    fun hasContentScheme(uri: Uri): Boolean {
        return CONTENT_SCHEME == uri.scheme
    }

    @JvmStatic
    fun close(c: Closeable?) {
        if (c != null) { // java.lang.IncompatibleClassChangeError: interface not implemented
            try {
                c.close()
            } catch (e: IOException) {
                // silence
            }
        }
    }
}