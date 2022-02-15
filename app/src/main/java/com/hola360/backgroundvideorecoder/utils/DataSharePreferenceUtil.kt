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

    fun saveUriSdCard(value: String) {
        putStringValue("key_sdcard", value)
    }

    fun getUriSdCard(): String {
        return getStringValue("key_sdcard")?:""
    }

    fun putVideoConfiguration(value: String) {
        putStringValue("Video_configuration", value)
    }

    fun getVideoConfiguration(): String? {
        return getStringValue("Video_configuration")
    }

    fun putSchedule(value: String) {
        putStringValue("Record_schedule", value)
    }

    fun getSchedule(): String? {
        return getStringValue("Record_schedule")
    }

    fun putGeneralSetting(value: String){
        putStringValue("General_setting", value)
    }

    fun getGeneralSetting():String?{
        return getStringValue("General_setting")
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

    fun putLongValue(key: String?, value: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(key, value).apply()
    }

    fun getLongValue(key: String?): Long {
        return sharedPreferences.getLong(key, 0L)
    }

    //Audio
    fun setAudioConfig(value: String) {
        putStringValue("Audio_config", value)
    }

    fun getAudioConfig(): String? {
        return getStringValue("Audio_config")
    }

    fun setAudioSchedule(value: String?) {
        putStringValue("Audio_schedule", value)
    }

    fun getAudioSchedule(): String? {
        return getStringValue("Audio_schedule")
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