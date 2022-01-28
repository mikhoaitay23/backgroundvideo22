package com.hola360.backgroundvideorecoder.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.camera.video.Recording
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.app.App
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.video.RecordVideo
import com.hola360.backgroundvideorecoder.ui.record.video.model.CustomLifeCycleOwner
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import android.app.Activity
import android.content.Context


class RecordService : Service() {

    private lateinit var listener: Listener
    private val previewVideoWindow:PreviewVideoWindow by lazy {
        PreviewVideoWindow(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        recordVideo(intent)
        sendNotification()
        return START_NOT_STICKY
    }

    private fun recordVideo(intent: Intent?){
        intent?.let {
            val configuration= it.getParcelableExtra<VideoRecordConfiguration>("Video_configuration")
            when(it.getIntExtra("Video_status", 0)){
                RecordVideo.START->{
                    if (configuration != null) {
                        previewVideoWindow.setupVideoConfiguration(configuration)
                        previewVideoWindow.open()
                        previewVideoWindow.startRecording()
                    }
                }
                RecordVideo.CLEAR->{
                    previewVideoWindow.close()
                    stopSelf()
                }
                RecordVideo.PAUSE->{
                    previewVideoWindow.pauseAndResume()
                }
            }
        }
    }

    private fun sendNotification() {
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_main_graph)
            .setDestination(R.id.nav_video_record)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, App.CHANNEL_SERVICE_ID)
            .setContentTitle("Record")
            .setContentText("test")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_schedule)
            .build()

        startForeground(1, notification)
    }

    fun registerListener(activity: Activity) {
        this.listener = activity as Listener
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    interface Listener{
        fun updateData()
    }
}