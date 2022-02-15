package com.example.techtallfeb2022.data.model

import android.content.Context
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.StorageId

import com.google.android.gms.common.util.Base64Utils
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.model.StorageModel
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.nio.charset.Charset

@Parcelize
data class StorageBrowserModel(
    val storageModels: MutableList<StorageModel>,
    var curStorage: Int
) :
    Parcelable {
    @IgnoredOnParcel
    val curStack = mutableListOf<String>()

    private fun getSubPath() = if (curStack.isEmpty()) {
        ""
    } else {
        var path = ""
        curStack.forEach { item ->
            path = path.plus(File.separator)
            path = path.plus(item)
        }
        path
    }

    fun getCurDocumentationFile(context: Context): DocumentFile? {
        return DocumentFileCompat.fromSimplePath(
            context,
            curStorageModel().storageId,
            getSubPath()
        )
    }

    fun curStorageModel() = storageModels[curStorage]

    fun fullPathStack(context: Context): MutableList<String> {
        val paths = mutableListOf(
            if (curStorageModel().storageId == StorageId.PRIMARY) {
                context.getString(R.string.internal_storage)
            } else {
                context.getString(R.string.sd_card)
            }
        )
        if (curStack.isNotEmpty()) {
            paths.addAll(curStack)
        }
        return paths
    }

    fun fullPath(context: Context): String {
        val paths =
            if (curStorageModel().storageId == StorageId.PRIMARY) {
                context.getString(R.string.internal_storage)
            } else {
                context.getString(R.string.sd_card)
            }
        return Base64Utils.encode(
            if (curStack.isNotEmpty()) {
                paths.plus(getSubPath())
            } else {
                paths
            }.toByteArray(Charset.defaultCharset())
        )
    }

}