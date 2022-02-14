package com.hola360.backgroundvideorecoder.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        createChannelNotification()
    }

    private fun createChannelNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel= NotificationChannel(CHANNEL_SERVICE_ID, "Channel",
            NotificationManager.IMPORTANCE_NONE)
            val notificationManager= getSystemService(NotificationManager::class.java)
            notificationManager?.let {
                it.createNotificationChannel(channel)
            }
        }
    }

    companion object{
        const val CHANNEL_SERVICE_ID= "channel_id"
    }
}