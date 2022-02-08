package com.hola360.backgroundvideorecoder.ui.record.video.model

import android.os.Parcel
import android.os.Parcelable

class VideoRecordConfiguration() : Parcelable {
    var isBack:Boolean= true
    var cameraQuality:Int=0
    var zoomScale:Float=0f
    var totalTime:Long=0
    var timePerVideo:Long=60000L
    var previewMode:Boolean= true
    var flash:Boolean= false
    var sound:Boolean= true
    var scheduleTime:Long=0L

    constructor(parcel: Parcel) : this() {
        isBack = parcel.readByte() != 0.toByte()
        cameraQuality = parcel.readInt()
        totalTime = parcel.readLong()
        timePerVideo = parcel.readLong()
        previewMode = parcel.readByte() != 0.toByte()
        flash = parcel.readByte() != 0.toByte()
        sound = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isBack) 1 else 0)
        parcel.writeInt(cameraQuality)
        parcel.writeLong(totalTime)
        parcel.writeLong(timePerVideo)
        parcel.writeByte(if (previewMode) 1 else 0)
        parcel.writeByte(if (flash) 1 else 0)
        parcel.writeByte(if (sound) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoRecordConfiguration> {
        override fun createFromParcel(parcel: Parcel): VideoRecordConfiguration {
            return VideoRecordConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<VideoRecordConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}