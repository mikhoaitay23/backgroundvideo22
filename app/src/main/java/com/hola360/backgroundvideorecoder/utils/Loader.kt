package com.hola360.backgroundvideorecoder.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.hola360.backgroundvideorecoder.data.model.media.AudioFile
import java.io.File

object Loader {
    fun getAudios(context: Context): MutableList<AudioFile> {
        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        return getListFromUri(context, uri)
    }

    @SuppressLint("Range")
    private fun getListFromUri(context: Context, uri: Uri): MutableList<AudioFile> {
        val list = mutableListOf<AudioFile>()
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.RELATIVE_PATH,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Audio.Media.BUCKET_ID,
                MediaStore.Audio.Media.DURATION
            )
        } else {
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
            )
        }
        val selectionArgs = arrayOf(
            "audio/mp3",
            "%BgRecord/Audio%"
        )
        val selection =
            MediaStore.Audio.Media.MIME_TYPE + "=? and " + MediaStore.Audio.Media.DATA + " like ?"
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        val query = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    uri,
                    id
                )
                val name = cursor.getString(nameColumn)
                val path =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        PathUtils.getPath(context, contentUri)
                    } else {
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    }
                val file = File(path)
                if (!file.exists() or file.isHidden) continue
                val myFile = AudioFile(
                    name,
                    contentUri, null,
                    file.length(),
                    file.lastModified()
                )
                list += myFile
            }
            cursor.close()
        }
        return list
    }
}