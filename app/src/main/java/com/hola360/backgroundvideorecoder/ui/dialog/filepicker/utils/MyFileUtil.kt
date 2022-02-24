package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils


import android.webkit.MimeTypeMap

import com.hola360.backgroundvideorecoder.BuildConfig
import java.io.File


object MyFileUtil {
    private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"
    fun getAllFolderAndFile(file: File): MutableList<File> {
        val filesList = mutableListOf<File>()

        if (!file.listFiles().isNullOrEmpty()) {
            filesList.addAll(file.listFiles()!!.filter {
                !it.isHidden
            })
        }
        return filesList
    }

    fun getAllFileOnly(file: File): MutableList<File> {
        val fileList = mutableListOf<File>()

        if (!file.listFiles().isNullOrEmpty()) {
            fileList.addAll(file.listFiles()!!.filter {
                !it.isHidden && it.isFile
            })
            fileList.sortByDescending {
                it.lastModified()
            }
        }
        return fileList
    }

    private fun getMimeType(cFile: File): String =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(cFile.extension)
            ?.lowercase()
            ?: "unknown"
}

