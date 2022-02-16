package com.hola360.backgroundvideorecoder.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.app.App
import com.hola360.backgroundvideorecoder.broadcastreciever.BatteryLevelReceiver
import com.hola360.backgroundvideorecoder.broadcastreciever.ListenRecordScheduleBroadcast
import com.hola360.backgroundvideorecoder.service.notification.RecordNotificationManager
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.SoundRecorder
import com.hola360.backgroundvideorecoder.utils.*
import java.util.*


class RecordService : Service() {

    private val notificationManager: NotificationManager by lazy {
        this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val recordScheduleBroadcast: ListenRecordScheduleBroadcast by lazy {
        ListenRecordScheduleBroadcast()
    }
    private val batteryLevelReceiver: BatteryLevelReceiver by lazy {
        BatteryLevelReceiver()
    }
    private val mRecordNotificationManager by lazy {
        RecordNotificationManager(this)
    }
    private var notificationTitle: String = ""
    private var notificationContent: String = ""
    var recordStatus: Int = MainActivity.NO_RECORDING
    var mBinder = LocalBinder()

    private var listener: Listener? = null
    private var time = 0L
    var mSoundRecorder: SoundRecorder? = null
    private var mServiceManager: ServiceManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        time = time.plus(TIME_LOOP)
        mServiceManager!!.updateProgress(Utils.convertTime(time / 1000))
        listener?.onUpdateTime("Info", 0, time)
        nextLoop()
    }

    private val previewVideoWindow: PreviewVideoWindow by lazy {
        PreviewVideoWindow(this, object : PreviewVideoWindow.RecordAction {
            override fun onRecording(time: Long, isComplete: Boolean) {
                if (recordStatus != MainActivity.NO_RECORDING) {
                    listener?.updateRecordTime(time, MainActivity.RECORD_VIDEO)
                    notificationContent = VideoRecordUtils.generateRecordTime(time)
                    notificationManager.notify(NOTIFICATION_ID, getNotification())
                }
            }

            override fun onStopRecordWhenLowMemory() {
                recordStatus = MainActivity.NO_RECORDING
                VideoRecordUtils.checkScheduleWhenRecordStop(this@RecordService)
                stopForeground(true)
                notificationManager.cancel(NOTIFICATION_ID)
            }

            override fun onFinishRecord() {
                listener?.onRecordCompleted()
                notificationTitle =
                    this@RecordService.resources.getString(R.string.video_record_complete_prefix)
                VideoRecordUtils.checkScheduleWhenRecordStop(this@RecordService)
                notificationManager.notify(NOTIFICATION_ID, getNotification())
                stopForeground(true)
            }
        })
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    private inner class ServiceManager {
        fun startRecord() {
            val notification = mRecordNotificationManager.getNotification(
                notificationTitle,
                notificationContent
            )
            startForeground(RecordNotificationManager.NOTIFICATION_ID, notification)
        }

        fun startSchedule(time: Long) {
            val notification = mRecordNotificationManager.getNotification(
                notificationTitle,
                notificationContent
            )
            startForeground(RecordNotificationManager.NOTIFICATION_ID, notification)
        }

        fun updateProgress(time: String) {
            val notification = mRecordNotificationManager.getNotification(
                notificationTitle,
                time
            )
            mRecordNotificationManager.notificationManager.notify(
                RecordNotificationManager.NOTIFICATION_ID,
                notification
            )
        }

        fun error() {
            stopForeground(true)
        }

        fun stop() {
            stopForeground(true)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val scheduleFilter = IntentFilter().apply {
            addAction(Constants.SCHEDULE_TYPE)
        }
//        registerReceiver(recordScheduleBroadcast, intentFilter)
        val batteryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
        }
        registerReceiver(recordScheduleBroadcast, scheduleFilter)
        registerReceiver(batteryLevelReceiver, batteryFilter)
        mServiceManager = ServiceManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun startRecordAudio() {
        if (!isRecording()) {
            val mp3Name = String.format(
                Configurations.TEMPLATE_AUDIO_FILE_NAME,
                DateTimeUtils.getFullDate(Date().time)
            )
            mSoundRecorder =
                SoundRecorder(this, mp3Name, 8000, object : SoundRecorder.OnRecorderListener {
                    override fun onBuffer(buf: ShortArray?, minBufferSize: Int) {
                        listener?.onByteBuffer(buf, minBufferSize)
                    }

                })
            mSoundRecorder!!.setHandle(object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        SoundRecorder.MSG_REC_STARTED -> {
                            mServiceManager!!.startRecord()
                        }
                        SoundRecorder.MSG_REC_STOPPED -> {
                            mServiceManager!!.stop()
                            listener?.onStopped()
                        }
                        SoundRecorder.MSG_ERROR_GET_MIN_BUFFER_SIZE, SoundRecorder.MSG_ERROR_CREATE_FILE, SoundRecorder.MSG_ERROR_REC_START, SoundRecorder.MSG_ERROR_AUDIO_RECORD, SoundRecorder.MSG_ERROR_AUDIO_ENCODE, SoundRecorder.MSG_ERROR_WRITE_FILE, SoundRecorder.MSG_ERROR_CLOSE_FILE -> {
                            recordAudioFailed()
                        }
                    }
                }
            })
            time = 0
            mSoundRecorder!!.start()
            nextLoop()
        }

    }

    fun stopRecording() {
        mServiceManager!!.stop()
        if (mSoundRecorder != null && mSoundRecorder!!.isRecording()) {
            mSoundRecorder!!.stop()
        }
        handler.removeCallbacks(runnable)
    }

    private fun nextLoop() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, TIME_LOOP)
    }

    private fun recordAudioFailed() {
        ToastUtils.getInstance(this@RecordService)!!.showToast("Error")
        mServiceManager!!.error()
        listener?.onStopped()
        handler.removeCallbacks(runnable)
    }

    fun isRecording(): Boolean {
        return mSoundRecorder != null && mSoundRecorder!!.isRecording()
    }

    fun getRecordState(): RecordState {
        return if ((mSoundRecorder != null && mSoundRecorder!!.isRecording())) {
            RecordState.AudioRecording
        } else {
            RecordState.None
        }
    }

    private fun getNotification(): Notification {
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_main_graph)
            .setDestination(R.id.nav_video_record)
            .createPendingIntent()

        return NotificationCompat.Builder(this, App.NONE_SERVICE_CHANNEL_ID)
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

        fun updateRecordTime(time: Long, status: Int)

        fun onRecordCompleted()

        fun onUpdateTime(fileName: String, duration: Long, curTime: Long)

        fun onStopped()

        fun onByteBuffer(buf: ShortArray?, minBufferSize: Int)
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val TIME_LOOP = 500L
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(recordScheduleBroadcast)
        unregisterReceiver(batteryLevelReceiver)
        Log.d("abcVideo", "Service killed")
    }

    enum class RecordState {
        None, AudioRecording, VideoRecording
    }

}