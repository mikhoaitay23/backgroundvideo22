package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.repository

import android.content.Context
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.model.StorageModel
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.MyFileUtil
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InternalRepository(val context: Context) {

    suspend fun getInternalFiles(cFile: File): List<File> = withContext(Dispatchers.IO) {
        MyFileUtil.getAllFolderAndFile(cFile)
    }

    suspend fun getAllStorages(): MutableList<StorageModel> = withContext(Dispatchers.Default) {
        StorageUtils.getAllStorages(context)
    }
}