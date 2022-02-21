package com.hola360.backgroundvideorecoder.data.model.media

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioFile(
    var title: String?,
    var uri: Uri?,
    var internalPath: String?,
    var size: Long,
    var date: Long
) : Parcelable