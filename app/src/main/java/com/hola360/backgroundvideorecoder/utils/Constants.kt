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
    val TAKE_PICTURE_PERMISSION = arrayOf(
        Manifest.permission.CAMERA
    )

    val TIME_ZONE_OFFSET= TimeZone.getDefault().getOffset(Date().time)
    const val  DELETE_DIALOG= "DeleteDialog"
    const val ASSETS_PATH: String= "file:///android_asset/"
    const val PHOTO_PATH: String = "ScanQrCode"

}