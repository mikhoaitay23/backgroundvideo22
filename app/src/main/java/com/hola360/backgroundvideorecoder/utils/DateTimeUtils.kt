package com.hola360.backgroundvideorecoder.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private const val TIME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
    fun getFullDate(time: Long): String? {
        val sdfDate = SimpleDateFormat(TIME_FORMAT, Locale.US)
        return sdfDate.format(time)
    }
}