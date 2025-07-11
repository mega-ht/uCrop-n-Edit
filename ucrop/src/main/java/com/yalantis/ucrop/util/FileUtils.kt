/*
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yalantis.ucrop.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale

/**
 * @author Peli
 * @author paulburke (ipaulpro)
 * @version 2013-12-11
 */
object FileUtils {
    /**
     * TAG for log messages.
     */
    private const val TAG = "FileUtils"

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    fun isExternalStorageDocument(uri: Uri): Boolean = "com.android.externalstorage.documents" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    fun isDownloadsDocument(uri: Uri): Boolean = "com.android.providers.downloads.documents" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    fun isMediaDocument(uri: Uri): Boolean = "com.android.providers.media.documents" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean = "com.google.android.apps.photos.content" == uri.authority

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection =
            arrayOf(
                column,
            )
        try {
            cursor =
                context.contentResolver.query(
                    uri!!,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (ex: IllegalArgumentException) {
            Log.i(
                TAG,
                String.format(Locale.getDefault(), "getDataColumn: _data - [%s]", ex.message),
            )
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    fun getPath(
        context: Context,
        uri: Uri,
    ): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                if (!TextUtils.isEmpty(id)) {
                    return try {
                        val contentUri =
                            ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                java.lang.Long.valueOf(id),
                            )
                        getDataColumn(context, contentUri, null, null)
                    } catch (e: NumberFormatException) {
                        Log.i(TAG, e.message!!)
                        null
                    }
                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs =
                    arrayOf(
                        split[1],
                    )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(uri)) {
                uri.lastPathSegment
            } else {
                getDataColumn(context, uri, null, null)
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Copies one file into the other with the given Uris.
     * In the event that the Uris are the same, trying to copy one file to the other
     * will cause both files to become null.
     * Simply skipping this step if the paths are identical.
     *
     * @param context The context from which to require the [android.content.ContentResolver]
     * @param uriFrom Represents the source file
     * @param uriTo Represents the destination file
     */
    @JvmStatic
    @Throws(IOException::class)
    fun copyFile(
        context: Context,
        uriFrom: Uri,
        uriTo: Uri,
    ) {
        if (uriFrom == uriTo) {
            return
        }

        var isFrom: InputStream? = null
        var osTo: OutputStream? = null
        try {
            isFrom = context.contentResolver.openInputStream(uriFrom)
            osTo = context.contentResolver.openOutputStream(uriTo)

            if (isFrom is FileInputStream && osTo is FileOutputStream) {
                val inputChannel = isFrom.channel
                val outputChannel = osTo.channel
                inputChannel.transferTo(0, inputChannel.size(), outputChannel)
            } else {
                throw java.lang.IllegalArgumentException(
                    "The input or output URI don't represent a file. " +
                        "uCrop requires then to represent files in order to work properly.",
                )
            }
        } finally {
            isFrom?.close()
            osTo?.close()
        }
    }
}
