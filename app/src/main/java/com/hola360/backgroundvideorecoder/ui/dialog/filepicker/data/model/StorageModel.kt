package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.model

import android.content.Context
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StorageModel(
    val storageId: String, val total: Long, val freed: Long
) : Parcelable {
    fun getRootDocumentationFile(context: Context): DocumentFile {
        return DocumentFile.fromFile(
            DocumentFileCompat.getRootRawFile(
                context,
                storageId,
                false
            )!!
        )
    }

    fun getPercentage(): Int {
        return ((total - freed) * 100 / total).toInt()
    }

    fun getUsedString(): String {
        return FilePickerUtils.humanReadableByteCountBin(
            total - freed,
            FACTOR
        )
    }

    fun getTotalString(): String {
        return FilePickerUtils.humanReadableByteCountBin(
            total,
            FACTOR
        )
    }


    fun getAvailableString(): String {
        return FilePickerUtils.humanReadableByteCountBin(freed, FACTOR)
    }

    companion object {
        const val FACTOR = 1000f
    }
}
