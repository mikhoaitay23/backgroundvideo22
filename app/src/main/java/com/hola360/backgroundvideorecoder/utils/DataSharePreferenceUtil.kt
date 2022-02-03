package com.hola360.backgroundvideorecoder.utils

import android.content.Context
import android.content.SharedPreferences

class DataSharePreferenceUtil private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun putStringValue(key: String?, value: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value).apply()
    }

    fun getStringValue(key: String?): String? {
        return sharedPreferences.getString(key, "")
    }

    fun setVideoRecordConfiguration(value: String) {
        putStringValue("Video_configuration", value)
    }

    fun getVideoConfiguration(): String? {
        return getStringValue("Video_configuration")
    }

    fun putBooleanValue(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value).apply()
    }

    fun getBooleanValue(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putIntValue(key: String?, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value).apply()
    }

    fun getIntValue(key: String?): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun getCorrectionValue(key: String?): Int {
        return sharedPreferences.getInt(key, 1)
    }

    fun putLongValue(key: String?, value: Long){
        val editor = sharedPreferences.edit()
        editor.putLong(key, value).apply()
    }

    fun getLongValue(key: String?): Long {
        return sharedPreferences.getLong(key, 0L)
    }

    companion object {
        const val PREFERENCE_NAME = "FakeLive_pref"
        private var instance: DataSharePreferenceUtil? = null
        fun getInstance(context: Context): DataSharePreferenceUtil? {
            if (instance == null) {
                instance = DataSharePreferenceUtil(context)
            }
            return instance
        }
    }

}