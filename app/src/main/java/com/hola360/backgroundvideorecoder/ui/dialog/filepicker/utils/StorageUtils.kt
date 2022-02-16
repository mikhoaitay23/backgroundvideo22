package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.StorageId
import com.anggrayudi.storage.file.getBasePath
import com.anggrayudi.storage.file.inSdCardStorage
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.model.StorageBrowserModel
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.model.StorageModel


object StorageUtils {
    fun getAllStorages(context: Context): MutableList<StorageModel> {
        val storageList = mutableListOf<StorageModel>()
        val storageIds = DocumentFileCompat.getStorageIds(context)
        storageIds.forEach {
            val primaryStorage = StorageModel(it, getTotal(context, it), getFree(context, it))
            storageList.add(primaryStorage)
        }
        return storageList
    }

    private fun getTotal(context: Context, storageId: String): Long {
        return when (storageId) {
            StorageId.PRIMARY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val ssm =
                        context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                    ssm.getTotalBytes(StorageManager.UUID_DEFAULT)
                } else {
                    val stat = StatFs(Environment.getDataDirectory().path)
                    stat.blockCountLong * stat.blockSizeLong
                }
            }
            else -> {
                DocumentFileCompat.getStorageCapacity(context, storageId)
            }
        }
    }

    private fun getFree(context: Context, storageId: String): Long {
        return when (storageId) {
            StorageId.PRIMARY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val ssm =
                        context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                    ssm.getFreeBytes(StorageManager.UUID_DEFAULT)
                } else {
                    val stat = StatFs(Environment.getDataDirectory().path)
                    stat.blockSizeLong * stat.availableBlocksLong
                }
            }
            else -> {
                DocumentFileCompat.getFreeSpace(context, storageId)
            }
        }
    }


    fun checkIfSDCardRoot(uri: Uri): Boolean {
        return isExternalStorageDocument(uri) && isRootUri(uri) && !isInternalStorage(uri)
    }

    private fun isRootUri(uri: Uri): Boolean {
        val docId = DocumentsContract.getTreeDocumentId(uri)
        return docId.endsWith(":")
    }

    private fun isInternalStorage(uri: Uri): Boolean {
        return isExternalStorageDocument(uri) && DocumentsContract.getTreeDocumentId(uri)
            .contains("primary")
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun createStorageBrowserModel(
        context: Context,
        storageList: MutableList<StorageModel>,
        documentFile: DocumentFile
    ): StorageBrowserModel {
        val storageBrowserModel = StorageBrowserModel(
            storageList, if (!documentFile.inSdCardStorage(context)) {
                0
            } else {
                1
            }
        )
        val basePath = documentFile.getBasePath(context)
        val subPaths = basePath.split(Regex("/"))
        if (!subPaths.isNullOrEmpty()) {
            subPaths.forEach {
                storageBrowserModel.curStack.add(it)
            }
        }
        return storageBrowserModel
    }

    fun getSdCardAccessIntent(context: Context): Intent?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sdCardRootAccessIntent(context)
        } else {
            externalStorageRootAccessIntent(context)
        }
    }

    private fun sdCardRootAccessIntent(context: Context): Intent? {
        val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sm.storageVolumes.firstOrNull { it.isRemovable }?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.createOpenDocumentTreeIntent()
                } else {
                    if (it.isPrimary) {
                        SimpleStorage.defaultExternalStorageIntent
                    } else {
                        it.createAccessIntent(null)
                    }

                }
            } ?: SimpleStorage.defaultExternalStorageIntent
        } else {
            null
        }
    }

    private fun externalStorageRootAccessIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            sm.primaryStorageVolume.createOpenDocumentTreeIntent()
        } else {
            SimpleStorage.defaultExternalStorageIntent
        }
    }

}