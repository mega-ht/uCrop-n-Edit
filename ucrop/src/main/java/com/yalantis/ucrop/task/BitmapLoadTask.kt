package com.yalantis.ucrop.task


/**
 * Creates and returns a Bitmap for a given Uri(String url).
 * inSampleSize is calculated based on requiredWidth property. However can be adjusted if OOM occurs.
 * If any EXIF config is found - bitmap is transformed properly.
 */
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import com.yalantis.ucrop.UCropHttpClientStore
import com.yalantis.ucrop.callback.BitmapLoadCallback
import com.yalantis.ucrop.model.ExifInfo
import com.yalantis.ucrop.util.BitmapLoadUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.BufferedSource
import okio.sink
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class BitmapLoadTask(
    private val mContext: Context,
    inputUri: Uri,
    outputUri: Uri,
    requiredWidth: Int,
    requiredHeight: Int,
    loadCallback: BitmapLoadCallback
) {
    private var mInputUri: Uri = inputUri
    private val mOutputUri: Uri = outputUri
    private val mRequiredWidth: Int = requiredWidth
    private val mRequiredHeight: Int = requiredHeight
    private val mBitmapLoadCallback: BitmapLoadCallback = loadCallback

    class BitmapWorkerResult {
        var mBitmapResult: Bitmap? = null
        var mExifInfo: ExifInfo? = null
        var mBitmapWorkerException: Exception? = null

        constructor(bitmapResult: Bitmap, exifInfo: ExifInfo) {
            mBitmapResult = bitmapResult
            mExifInfo = exifInfo
        }

        constructor(bitmapWorkerException: Exception) {
            mBitmapWorkerException = bitmapWorkerException
        }
    }

    fun execute() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    processInputUri()
                } catch (e: Exception) {
                    return@withContext BitmapWorkerResult(e)
                }

                val options = BitmapFactory.Options().apply {
                    BitmapLoadUtils.decodeDimensions(mContext, mInputUri, this)
                    inSampleSize = BitmapLoadUtils.calculateInSampleSize(this, mRequiredWidth, mRequiredHeight)
                    inJustDecodeBounds = false
                }

                var decodeSampledBitmap: Bitmap? = null
                var decodeAttemptSuccess = false

                while (!decodeAttemptSuccess) {
                    try {
                        mContext.contentResolver.openInputStream(mInputUri).use { stream ->
                            decodeSampledBitmap = BitmapFactory.decodeStream(stream, null, options)
                            if (options.outWidth == -1 || options.outHeight == -1) {
                                return@withContext BitmapWorkerResult(IllegalArgumentException("Bounds for bitmap could not be retrieved from the Uri: [$mInputUri]"))
                            }
                        }
                        if (checkSize(decodeSampledBitmap, options)) continue
                        decodeAttemptSuccess = true
                    } catch (error: OutOfMemoryError) {
                        Log.e(TAG, "decodeBitmap: BitmapFactory.decodeStream: ", error)
                        options.inSampleSize *= 2
                    } catch (e: IOException) {
                        return@withContext BitmapWorkerResult(IllegalArgumentException("Bitmap could not be decoded from the Uri: [$mInputUri]", e))
                    }
                }

                if (decodeSampledBitmap == null) {
                    return@withContext BitmapWorkerResult(IllegalArgumentException("Bitmap could not be decoded from the Uri: [$mInputUri]"))
                }

                val exifOrientation = BitmapLoadUtils.getExifOrientation(mContext, mInputUri)
                val exifDegrees = BitmapLoadUtils.exifToDegrees(exifOrientation)
                val exifTranslation = BitmapLoadUtils.exifToTranslation(exifOrientation)
                val exifInfo = ExifInfo(exifOrientation, exifDegrees, exifTranslation)

                val matrix = Matrix().apply {
                    if (exifDegrees != 0) preRotate(exifDegrees.toFloat())
                    if (exifTranslation != 1) postScale(exifTranslation.toFloat(), 1f)
                }

                return@withContext if (!matrix.isIdentity) {
                    BitmapWorkerResult(BitmapLoadUtils.transformBitmap(decodeSampledBitmap!!, matrix), exifInfo)
                } else {
                    BitmapWorkerResult(decodeSampledBitmap!!, exifInfo)
                }
            }
            
            if (result.mBitmapWorkerException == null) {
                mBitmapLoadCallback.onBitmapLoaded(result.mBitmapResult!!, result.mExifInfo!!, mInputUri, mOutputUri)
            } else {
                mBitmapLoadCallback.onFailure(result.mBitmapWorkerException!!)
            }
        }
    }

    @Throws(NullPointerException::class, IOException::class, IllegalArgumentException::class)
    private suspend fun processInputUri() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Uri scheme: " + mInputUri.scheme)
        when {
            isDownloadUri(mInputUri) -> {
                try {
                    downloadFile(mInputUri, mOutputUri)
                } catch (e: Exception) {
                    Log.e(TAG, "Downloading failed", e)
                    throw e
                }
            }
            isContentUri(mInputUri) -> {
                try {
                    copyFile(mInputUri, mOutputUri)
                } catch (e: Exception) {
                    Log.e(TAG, "Copying failed", e)
                    throw e
                }
            }
            !isFileUri(mInputUri) -> {
                Log.e(TAG, "Invalid Uri scheme ${mInputUri.scheme}")
                throw IllegalArgumentException("Invalid Uri scheme ${mInputUri.scheme}")
            }
        }
    }

    private suspend fun copyFile(inputUri: Uri, outputUri: Uri?) = withContext(Dispatchers.IO) {
        Log.d(TAG, "copyFile")
        outputUri ?: throw NullPointerException("Output Uri is null - cannot copy image")

        mContext.contentResolver.openInputStream(inputUri)?.use { inputStream ->
            val outputStream = if (isContentUri(outputUri)) {
                mContext.contentResolver.openOutputStream(outputUri)
            } else {
                FileOutputStream(File(outputUri.path!!))
            }

            outputStream?.use { outStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outStream.write(buffer, 0, length)
                }
            }
        } ?: throw NullPointerException("InputStream for given input Uri is null")

        // Swap URIs after copying
        mInputUri = mOutputUri
    }

    private suspend fun downloadFile(inputUri: Uri, outputUri: Uri?) = withContext(Dispatchers.IO) {
        Log.d(TAG, "downloadFile")
        outputUri ?: throw NullPointerException("Output Uri is null - cannot download image")

        val client = UCropHttpClientStore.INSTANCE.client
        val request = Request.Builder().url(inputUri.toString()).build()

        client?.newCall(request)?.execute().use { response ->
            response ?: throw IOException("Failed to download file from the Uri: [$inputUri]")

            val source: BufferedSource? = response.body?.source()
            val outputStream: OutputStream? = if (isContentUri(outputUri)) {
                mContext.contentResolver.openOutputStream(outputUri)
            } else {
                FileOutputStream(File(outputUri.path!!))
            }

            outputStream?.use { sink ->
                source?.readAll(sink.sink())
            } ?: throw NullPointerException("OutputStream for given output Uri is null")

            // Swap URIs after downloading
            mInputUri = mOutputUri
        }
    }

    private fun checkSize(bitmap: Bitmap?, options: BitmapFactory.Options): Boolean {
        val bitmapSize = bitmap?.byteCount ?: 0
        return if (bitmapSize > MAX_BITMAP_SIZE) {
            options.inSampleSize *= 2
            true
        } else {
            false
        }
    }

    private fun isDownloadUri(uri: Uri): Boolean {
        val schema = uri.scheme
        return schema == "http" || schema == "https"
    }

    private fun isContentUri(uri: Uri): Boolean {
        val schema = uri.scheme
        return schema == "content"
    }

    private fun isFileUri(uri: Uri): Boolean {
        val schema = uri.scheme
        return schema == "file"
    }

    companion object {
        private const val TAG = "BitmapWorkerTask"
        private const val MAX_BITMAP_SIZE = 100 * 1024 * 1024 // 100 MB
    }
}
