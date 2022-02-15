package com.hola360.backgroundvideorecoder.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel
import com.hola360.backgroundvideorecoder.utils.DataSharePreferenceUtil
import com.hola360.backgroundvideorecoder.utils.Utils

class App: Application() {
    private val generalSetting: SettingGeneralModel by lazy {
        val dataPref= DataSharePreferenceUtil.getInstance(this)
        Utils.getDataPrefGeneralSetting(dataPref!!)
    }

    override fun onCreate() {
        super.onCreate()
        createNoneChannelNotification()
    }

    private fun createNoneChannelNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance= if(generalSetting.notificationImportance==0){
                NotificationManager.IMPORTANCE_DEFAULT
            }else{
                NotificationManager.IMPORTANCE_NONE
            }
            val channel= NotificationChannel(NONE_SERVICE_CHANNEL_ID, "Channel", importance)
            val notificationManager= getSystemService(NotificationManager::class.java)
            notificationManager?.let {
                it.createNotificationChannel(channel)
            }
        }
    }

    companion object{
        const val NONE_SERVICE_CHANNEL_ID= "channel_id"
    }
}