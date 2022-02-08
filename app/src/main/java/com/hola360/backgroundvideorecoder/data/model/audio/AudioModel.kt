package com.hola360.backgroundvideorecoder.data.model.audio

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioModel(
    var quality: AudioQuality = AudioQuality.MEDIUM,
    var mode: AudioMode = AudioMode.MONO,
    var duration: Long = 0,
    var isMuted: Boolean = false
) : Parcelable {

}
