package com.hola360.backgroundvideorecoder.service

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.app.App
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils
import com.hola360.backgroundvideorecoder.ui.record.video.RecordVideo
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration


class RecordService : Service(), AudioRecordUtils.Listener {

    private var listener: Listener? = null
    var mBinder = LocalBinder()

    private val previewVideoWindow: PreviewVideoWindow by lazy {
        PreviewVideoWindow(this, object : PreviewVideoWindow.RecordAction {
            override fun onFinishRecord() {
                stopSelf()
            }
        })
    }

    private var audioRecordUtils = AudioRecordUtils()

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        recordVideo(intent)
        return START_NOT_STICKY
    }

    private fun recordVideo(intent: Intent?) {
        intent?.let {
            val configuration =
                it.getParcelableExtra<VideoRecordConfiguration>("Video_configuration")
            when (it.getIntExtra("Video_status", 0)) {
                RecordVideo.START -> {
                    if (configuration != null) {
                        previewVideoWindow.setupVideoConfiguration(configuration)
                        previewVideoWindow.open()
                        previewVideoWindow.startRecording()
                    }
                }
                RecordVideo.CLEAR -> {
                    previewVideoWindow.close()
                    stopSelf()
                }
                RecordVideo.PAUSE -> {
                    previewVideoWindow.pauseAndResume()
                }
            }
        }
    }

    fun recordAudio(audioModel: AudioModel) {
        audioRecordUtils.registerListener(this)
        if (audioRecordUtils.isRecording()) {
            audioRecordUtils.onStopRecording()
            stopSelf()
        } else {
            audioRecordUtils.onStartRecording(audioModel)
            sendNotification()
        }
    }

    private fun sendNotification() {
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_main_graph)
            .setDestination(R.id.nav_video_record)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, App.CHANNEL_SERVICE_ID)
            .setContentTitle(getString(R.string.audio_record_is_running))
            .setContentText(getString(R.string.tap_to_open_now))
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_schedule)
            .build()

        startForeground(1, notification)
    }

    inner class LocalBinder : Binder() {
        fun getServiceInstance(): RecordService = this@RecordService
    }

    fun registerListener(activity: Activity?) {
        this.listener = activity as Listener
    }

    interface Listener {
        fun isStarted()
    }

    override fun updateTimer(time: Long) {

    }
}