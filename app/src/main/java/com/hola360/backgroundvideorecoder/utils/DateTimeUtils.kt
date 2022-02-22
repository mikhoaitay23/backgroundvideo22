package com.hola360.backgroundvideorecoder.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private const val TIME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
    fun getFullDate(time: Long): String? {
        val sdfDate = SimpleDateFormat(TIME_FORMAT, Locale.US)
        return sdfDate.format(time)
    }

    private const val TIME_MY_FILE_FLAG_FORMAT = "EEE, MMM dd, yyyy"
    private const val TIME_DATE_MY_FILE_FORMAT = "HH:mm - dd/MM/yyyy"

    fun getDateMyFileFlag(time: Long): String? {
        val sdfDate = SimpleDateFormat(TIME_MY_FILE_FLAG_FORMAT, Locale.US)
        return sdfDate.format(time)
    }

    fun getTimeDateMyFile(time: Long): String? {
        val sdfDate = SimpleDateFormat(TIME_DATE_MY_FILE_FORMAT, Locale.US)
        return sdfDate.format(time)
    }
}