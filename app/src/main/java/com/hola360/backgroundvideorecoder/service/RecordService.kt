package com.hola360.backgroundvideorecoder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.broadcastreciever.BatteryLevelReceiver
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.service.notification.RecordNotificationManager
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.SoundRecorder
import com.hola360.backgroundvideorecoder.utils.*
import java.util.*

class RecordService : Service() {

    private val batteryLevelReceiver: BatteryLevelReceiver by lazy {
        BatteryLevelReceiver()
    }
    private val mRecordNotificationManager by lazy {
        RecordNotificationManager(this)
    }
    private var notificationTitle: String = ""
    private var notificationContent: String = ""
    var mBinder = LocalBinder()
    private var videoPreviewVideoWindow: PreviewVideoWindow? = null
    private var listener: Listener? = null
    private var time = 0L
    private var recordState = RecordState.None
    var mSoundRecorder: SoundRecorder? = null
    private var mServiceManager: ServiceManager? = null
    private var mAudioModel: AudioModel? = null
    private val handler = Handler(Looper.getMainLooper())
    private var mp3Name: String? = null
    var isRecordScheduleStart = false
    private val runnable = Runnable {
        time = time.plus(TIME_LOOP)
        mServiceManager!!.updateProgress(Utils.convertTime(time / 1000))
        listener?.onUpdateTime(if (!mp3Name.isNullOrEmpty()) mp3Name!! else "Audio Record", 0, time)
        nextLoop()
        stopAudioRecordByTime(time)
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
        val batteryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
        }
        registerReceiver(batteryLevelReceiver, batteryFilter)
        mServiceManager = ServiceManager()
        initReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun initReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(RecordNotificationManager.ACTION_STOP)
        intentFilter.addAction(RecordNotificationManager.ACTION_RECORD_FROM_SCHEDULE)
        registerReceiver(globalReceiver, intentFilter)
    }

    private val globalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    RecordNotificationManager.ACTION_STOP -> {
                        stopRecording()
                    }
                    RecordNotificationManager.ACTION_RECORD_FROM_SCHEDULE -> {
                        val type = intent.getBooleanExtra(Constants.SCHEDULE_TYPE, false)
                        if (!type) {
                            startRecordAudio()
                            isRecordScheduleStart = true
                        }
                    }
                }

            }
        }

    }

    fun startRecordVideo() {
        if (recordState == RecordState.None) {
            mServiceManager!!.startRecord()
            notificationTitle = "Video recording"
            Log.d("abcVideo", "Start")
            videoPreviewVideoWindow =
                PreviewVideoWindow(this, object : PreviewVideoWindow.RecordAction {
                    override fun onRecording(recordTime: Long, isComplete: Boolean) {
                        notificationContent = if (isComplete) {
                            "Video record complete"
                        } else {
                            VideoRecordUtils.generateRecordTime(recordTime)
                        }
                        listener?.onUpdateTime("", 0L, recordTime)
                        val notification = mRecordNotificationManager.getNotification(
                            notificationTitle,
                            notificationContent
                        )
                        mRecordNotificationManager.notifyNewStatus(notification)
                    }

                    override fun onFinishRecord() {
                        listener?.onStopped()
                    }
                })
            videoPreviewVideoWindow!!.setupVideoConfiguration()
            videoPreviewVideoWindow!!.open()
            videoPreviewVideoWindow!!.startRecording()
            recordState = RecordState.VideoRecording
        }
    }

    fun stopRecordVideo() {
        if (recordState == RecordState.VideoRecording) {
            videoPreviewVideoWindow?.close()
            videoPreviewVideoWindow = null
            listener?.onStopped()
            recordState = RecordState.None
            mServiceManager!!.stop()
        }
    }

    fun updatePreviewVideoParams(visibility: Boolean) {

    }

    fun setVideoSchedule(scheduleTime: Long) {
        if (recordState == RecordState.None) {
            time = scheduleTime
            recordState = RecordState.VideoSchedule
        }
    }

    fun cancelVideoSchedule() {
        if (recordState == RecordState.VideoSchedule) {
            recordState = RecordState.None
            time = 0L
        }
    }

    fun startRecordAudio() {
        mAudioModel =
            if (!SharedPreferenceUtils.getInstance(this)?.getAudioConfig().isNullOrEmpty()) {
                Gson().fromJson(
                    SharedPreferenceUtils.getInstance(this)?.getAudioConfig(),
                    AudioModel::class.java
                )
            } else {
                AudioModel()
            }
        if (!isRecording()) {
            mp3Name = String.format(
                Configurations.TEMPLATE_AUDIO_FILE_NAME,
                DateTimeUtils.getFullDate(Date().time)
            )
            mSoundRecorder =
                SoundRecorder(
                    this,
                    mp3Name,
                    mAudioModel!!,
                    object : SoundRecorder.OnRecorderListener {
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
                            if (isRecordScheduleStart) {
                                SharedPreferenceUtils.getInstance(this@RecordService)!!
                                    .setAudioSchedule(null)
                            }
                            ToastUtils.getInstance(this@RecordService)!!
                                .showToast(getString(R.string.recording_stop))
                            recordState = RecordState.None
                        }
                        SoundRecorder.MSG_ERROR_GET_MIN_BUFFER_SIZE, SoundRecorder.MSG_ERROR_CREATE_FILE, SoundRecorder.MSG_ERROR_REC_START, SoundRecorder.MSG_ERROR_AUDIO_RECORD, SoundRecorder.MSG_ERROR_AUDIO_ENCODE, SoundRecorder.MSG_ERROR_WRITE_FILE, SoundRecorder.MSG_ERROR_CLOSE_FILE -> {
                            recordAudioFailed()
                        }
                    }
                }
            })
            time = 0
            recordState = RecordState.AudioRecording
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
        return recordState == RecordState.VideoRecording || recordState == RecordState.AudioRecording
    }

    fun getRecordState(): RecordState {
        return recordState
    }

    private fun stopAudioRecordByTime(time: Long) {
        if (mAudioModel!!.duration != 0L) {
            if (time == mAudioModel!!.duration) {
                stopRecording()
            }
        }
    }

    private fun getBroadcastPendingIntent(context: Context, isVideo: Boolean): PendingIntent {
        val intent = Intent(RecordNotificationManager.ACTION_RECORD_FROM_SCHEDULE).apply {
            putExtra(Constants.SCHEDULE_TYPE, isVideo)
        }
        return PendingIntent.getBroadcast(
            context, 0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun setAlarmSchedule(context: Context, time: Long, isVideo: Boolean) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (SystemUtils.isAndroidO()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                getBroadcastPendingIntent(context, isVideo)
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                time,
                getBroadcastPendingIntent(context, isVideo)
            )
        }
        mServiceManager!!.startSchedule(time)
    }

    fun cancelAlarmSchedule(context: Context, isVideo: Boolean) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getBroadcastPendingIntent(context, isVideo))
    }

    inner class LocalBinder : Binder() {
        fun getServiceInstance(): RecordService = this@RecordService
    }

    fun registerListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onUpdateTime(fileName: String, duration: Long, curTime: Long)

        fun onStopped()

        fun onByteBuffer(buf: ShortArray?, minBufferSize: Int)
    }

    companion object {
        const val TIME_LOOP = 500L
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(globalReceiver)
        unregisterReceiver(batteryLevelReceiver)
        Log.d("abcVideo", "Service killed")
    }

    enum class RecordState {
        None, AudioRecording, VideoRecording, AudioSchedule, VideoSchedule
    }

}