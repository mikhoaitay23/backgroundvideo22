package com.hola360.backgroundvideorecoder.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.app.App
import com.hola360.backgroundvideorecoder.broadcastreciever.ListenRecordScheduleBroadcast
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils
import com.hola360.backgroundvideorecoder.ui.record.video.ScheduleVideo
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import com.zlw.main.recorderlib.BuildConfig
import com.zlw.main.recorderlib.RecordManager
import java.util.*


class RecordService : Service(), AudioRecordUtils.Listener {

    private val notificationManager: NotificationManager by lazy {
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val recordScheduleBroadcast: ListenRecordScheduleBroadcast by lazy {
        ListenRecordScheduleBroadcast()
    }
    private var notificationTitle: String = ""
    private var notificationContent: String = ""
    var recordStatus: Int = MainActivity.NO_RECORDING
    private var listener: Listener? = null
    var mBinder = LocalBinder()
    private var recordManager = RecordManager.getInstance()
    private val previewVideoWindow: PreviewVideoWindow by lazy {
        PreviewVideoWindow(this, object : PreviewVideoWindow.RecordAction {
            override fun onRecording(time: Long, isComplete: Boolean) {
                notificationTitle = if (isComplete) {
                    this@RecordService.resources.getString(R.string.video_record_complete_prefix)
                } else {
                    this@RecordService.resources.getString(R.string.video_record_is_running)
                }
                listener?.updateRecordTime(time)
                if (recordStatus != MainActivity.NO_RECORDING) {
                    notificationContent = VideoRecordUtils.generateRecordTime(time)
                    notificationManager.notify(NOTIFICATION_ID, getNotification())
                }
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
        super.onCreate()
        val intentFilter = IntentFilter().apply {
            addAction(ScheduleVideo.SCHEDULE_VIDEO)
        }
        registerReceiver(recordScheduleBroadcast, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val status= it.getIntExtra("Video_status", 0)
            recordVideo(status)
        }
        return START_NOT_STICKY
    }

    fun recordVideo(status: Int) {
        when (status) {
            MainActivity.RECORD_VIDEO -> {
                previewVideoWindow.setupVideoConfiguration()
                previewVideoWindow.open()
                previewVideoWindow.startRecording()
                listener?.onRecordStarted(MainActivity.RECORD_VIDEO)
                recordStatus = MainActivity.RECORD_VIDEO
                notificationTitle = this.resources.getString(R.string.video_record_is_running)
                startForeground(NOTIFICATION_ID, getNotification())
            }
            MainActivity.STOP_VIDEO_RECORD -> {
                previewVideoWindow.close()
                recordStatus = MainActivity.NO_RECORDING
                stopForeground(true)
                notificationManager.cancel(NOTIFICATION_ID)
            }
            MainActivity.SCHEDULE_RECORD_VIDEO -> {
                notificationTitle =
                    this.getString(R.string.video_record_schedule_notification_title)
                notificationContent = VideoRecordUtils.generateScheduleTime(this)
                startForeground(NOTIFICATION_ID, getNotification())
            }
            MainActivity.CANCEL_SCHEDULE_RECORD_VIDEO -> {
                stopForeground(true)
            }
        }
    }

    fun recordAudio(status: Int, audioModel: AudioModel) {
        audioRecordUtils.registerListener(this)
        when (status) {
            MainActivity.AUDIO_RECORD -> {
                initRecord()
                audioRecordUtils.onStartRecording(audioModel)
                recordStatus = MainActivity.AUDIO_RECORD
                notificationTitle = this.resources.getString(R.string.audio_record_is_running)
                notificationContent = "haha"
                startForeground(NOTIFICATION_ID, getNotification())
                listener?.onRecordStarted(MainActivity.AUDIO_RECORD)
            }
            MainActivity.STOP_AUDIO_RECORD -> {
                audioRecordUtils.onStopRecording()
                recordStatus = MainActivity.NO_RECORDING
                stopForeground(true)
                notificationManager.cancel(NOTIFICATION_ID)
            }
        }
    }

    private fun initRecord() {
        recordManager!!.init(application, BuildConfig.DEBUG)
        val recordDir = String.format(
            Locale.getDefault(), "%s/Record/backgroundrecord/",
            Environment.getExternalStorageDirectory().absolutePath
        )
        recordManager.changeRecordDir(recordDir)
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
        fun onRecordStarted(status: Int)

        fun updateRecordTime(time: Long)

        fun onRecordCompleted()
    }

    override fun updateTimer(time: Long) {

    }

    companion object {
        const val NOTIFICATION_ID = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(recordScheduleBroadcast)
    }
}