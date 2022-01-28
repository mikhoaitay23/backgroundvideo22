package com.hola360.backgroundvideorecoder.utils

import android.os.Build
import android.os.Environment
import java.io.File
import java.util.*

object Utils {

    fun isAndroidQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun getDocumentationFolder(): File {
        val root = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)!!.path.plus(
                File.separator
            )
                .plus(Constants.FOLDER_NAME)
        )

        if (!root.exists()) {
            root.mkdirs()
        }
        return root
    }

    fun formatTimeIntervalHourMinSec(mills: Long): String? {
        val hour = mills / (60 * 60 * 1000)
        val min = mills / (60 * 1000) % 60
        val sec = mills / 1000 % 60
        return if (hour == 0L) {
            if (min == 0L) {
                String.format(Locale.getDefault(), "00:00:%02d", sec)
            } else {
                String.format(Locale.getDefault(), "00:%02d:%02d", min, sec)
            }
        } else {
            String.format(Locale.getDefault(), "%02dh:%02dm:%02ds", hour, min, sec)
        }
    }
}