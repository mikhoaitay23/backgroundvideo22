package com.hola360.backgroundvideorecoder.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        createNoneChannelNotification()
    }

    private fun createNoneChannelNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel= NotificationChannel(NONE_SERVICE_CHANNEL_ID, "Channel_silence",
            NotificationManager.IMPORTANCE_NONE)
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