package com.hola360.backgroundvideorecoder.data.model.storage

import android.content.Context
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.hola360.backgroundvideorecoder.utils.Utils
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
        return Utils.humanReadableByteCountBin(
            total - freed,
            FACTOR
        )
    }

    fun getTotalString(): String {
        return Utils.humanReadableByteCountBin(
            total,
            FACTOR
        )
    }


    fun getAvailableString(): String {
        return Utils.humanReadableByteCountBin(freed, FACTOR)
    }

    companion object {
        const val FACTOR = 1000f
    }
}
