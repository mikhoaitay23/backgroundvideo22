package com.hola360.backgroundvideorecoder.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.app.App
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils
import com.hola360.backgroundvideorecoder.ui.record.video.RecordVideo
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils


class RecordService : Service(), AudioRecordUtils.Listener {

    private val notificationManager: NotificationManager by lazy {
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private var notificationTitle: String = ""
    private var notificationContent: String = ""
    var recordStatus:Int= MainActivity.NO_RECORDING
    private var listener: Listener? = null
    var mBinder = LocalBinder()
    private val previewVideoWindow: PreviewVideoWindow by lazy {
        PreviewVideoWindow(this, object : PreviewVideoWindow.RecordAction {
            override fun onRecording(time: Long, isComplete:Boolean) {
                notificationTitle= if(isComplete){
                    this@RecordService.resources.getString(R.string.video_record_complete_prefix)
                }else{
                    this@RecordService.resources.getString(R.string.video_record_is_running)
                }
                listener?.updateRecordTime(time)
                notificationContent = VideoRecordUtils.generateRecordTime(time)
                notificationManager.notify(NOTIFICATION_ID, getNotification())
            }

            override fun onFinishRecord() {
                listener?.onRecordCompleted()
                stopForeground(true)
            }
        })
    }
    private var audioRecordUtils = AudioRecordUtils()

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        Log.d("abcVideo", "Service  oncreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        recordVideo(intent)
        Log.d("abcVideo", "Service  start")
        return START_NOT_STICKY
    }

    private fun recordVideo(intent: Intent?) {
        intent?.let {
            when (it.getIntExtra("Video_status", 0)) {
                RecordVideo.START -> {
                    previewVideoWindow.setupVideoConfiguration()
                    previewVideoWindow.open()
                    previewVideoWindow.startRecording()
                    listener?.onRecordStarted(MainActivity.VIDEO_RECORD)
                    recordStatus= MainActivity.VIDEO_RECORD
                    notificationTitle = this.resources.getString(R.string.video_record_is_running)
                    Log.d("abcVideo", "Service  start record")
                    startForeground(NOTIFICATION_ID, getNotification())
                }
                RecordVideo.CLEAR -> {
                    previewVideoWindow.close()
                    recordStatus=MainActivity.NO_RECORDING
                    stopForeground(true)
                    notificationManager.cancel(NOTIFICATION_ID)
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
//            sendNotification()
        }
    }

    private fun getNotification(): Notification {
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_main_graph)
            .setDestination(R.id.nav_video_record)
            .createPendingIntent()

        return NotificationCompat.Builder(this, App.CHANNEL_SERVICE_ID)
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_schedule)
            .build()
    }

    inner class LocalBinder : Binder() {
        fun getServiceInstance(): RecordService = this@RecordService
    }

    fun registerListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onRecordStarted(status:Int)

        fun updateRecordTime(time:Long)

        fun onRecordCompleted()
    }

    override fun updateTimer(time: Long) {

    }

    companion object {
        const val NOTIFICATION_ID = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("abcVideo", "Service  killed")
    }
}