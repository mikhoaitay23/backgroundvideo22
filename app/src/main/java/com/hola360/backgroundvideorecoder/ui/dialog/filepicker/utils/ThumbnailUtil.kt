package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils

import com.hola360.backgroundvideorecoder.R
import java.io.File

object ThumbnailUtil {

    fun getDefaultThumbnail(cFile: File): Int {
        return if (cFile.isDirectory)
            R.drawable.ic_folder
        else {
            R.drawable.ic_unknowfile
        }
    }


}