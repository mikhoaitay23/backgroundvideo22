package com.hola360.backgroundvideorecoder.data.repository

import android.app.Application
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.MyFileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BackgroundRecordRepository(val application: Application) {

    init {

    }

    suspend fun getAllFileOnly(file: File): MutableList<File> = withContext(Dispatchers.IO) {
        MyFileUtil.getAllFileOnly(file)
    }
}