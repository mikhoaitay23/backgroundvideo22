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

    private lateinit var listener: Listener
    var mBinder: IBinder = LocalBinder()

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
        recordAudio(intent)
        sendNotification(intent)
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

    private fun recordAudio(intent: Intent?) {
        intent?.let {
            audioRecordUtils.registerListener(this)
            val configuration =
                it.getParcelableExtra<AudioModel>("Audio_configuration")
            when (it.getIntExtra("Audio_status", 0)) {
                AudioRecordUtils.START -> {
                    if (configuration != null) {
                        if (audioRecordUtils.isRecording()) {
                            audioRecordUtils.onStopRecording()
                        } else
                            audioRecordUtils.onStartRecording(configuration)
                    }
                }
                AudioRecordUtils.PAUSE -> {

                }
                AudioRecordUtils.RESUME -> {

                }
                AudioRecordUtils.STOP -> {

                }
            }
        }
    }

    private fun sendNotification(intent: Intent?) {
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_main_graph)
            .setDestination(R.id.nav_video_record)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, App.CHANNEL_SERVICE_ID)
            .setContentTitle(
                if (intent!!.getIntExtra("Audio_status", 0) == 0) {
                    getString(R.string.audio_record_is_running)
                } else {
                    getString(R.string.video_record_is_running)
                }
            )
            .setContentText(getString(R.string.tap_to_open_now))
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_schedule)
            .build()

        startForeground(1, notification)
    }

    class LocalBinder : Binder() {
        fun getServiceInstance(): RecordService {
            return RecordService()
        }
    }

    fun registerListener(activity: Activity) {
        listener = activity as Listener
    }

    interface Listener {
        fun isStarted()
    }

    override fun updateTimer(time: Long) {

    }
}