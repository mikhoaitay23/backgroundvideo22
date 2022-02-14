package com.hola360.backgroundvideorecoder.utils

import android.Manifest
import java.util.*

object Constants {
    var STORAGE_PERMISSION_UNDER_STORAGE_SCOPE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var STORAGE_PERMISSION_STORAGE_SCOPE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val CAMERA_RECORD_PERMISSION = if(SystemUtils.isAndroidQ()){
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }else{
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }


    val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

    const val FOLDER_NAME = "BackgroundRecorder"

    const val RECORD_VIDEO_TYPE= "Record_type"
    const val VIDEO_STATUS= "Video_status"
    const val SCHEDULE_TYPE= "Schedule_type"

    const val RECORD_AUDIO_TYPE = "RECORD_AUDIO_TYPE"
    const val RECORD_AUDIO_STATUS = "AUDIO_STATUS"



    val TIME_ZONE_OFFSET = TimeZone.getDefault().getOffset(Date().time)
    const val DELETE_DIALOG = "DeleteDialog"
    const val ASSETS_PATH: String = "file:///android_asset/"
    const val PHOTO_PATH: String = "ScanQrCode"

}