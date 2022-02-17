package com.hola360.backgroundvideorecoder.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.broadcastreciever.BatteryLevelReceiver
import com.hola360.backgroundvideorecoder.broadcastreciever.ListenRecordScheduleBroadcast
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
    private var videoPreviewVideoWindow: PreviewVideoWindow?=null
    private var listener: Listener? = null
    var time = 0L
    private var recordStateLiveData= MutableLiveData<RecordState>()
    var mSoundRecorder: SoundRecorder? = null
    private var mServiceManager: ServiceManager? = null
    private var mAudioModel: AudioModel? = null
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        time = time.plus(TIME_LOOP)
        mServiceManager!!.updateProgress(Utils.convertTime(time / 1000))
        listener?.onUpdateTime("Info", 0, time)
        nextLoop()
        stopAudioRecordByTime(time)
    }
    private val scheduleReceiver = ListenRecordScheduleBroadcast()

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
        val batteryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
        }
        registerReceiver(scheduleReceiver, scheduleFilter)
        registerReceiver(batteryLevelReceiver, batteryFilter)
        mServiceManager = ServiceManager()
        recordStateLiveData.value= RecordState.None
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val status= it.getIntExtra(Constants.VIDEO_STATUS, 0)
            if(status == MainActivity.RECORD_VIDEO){
                startRecordVideo()
            }
        }
        return START_NOT_STICKY
    }

    fun startRecordVideo(){
        if(recordStateLiveData.value == RecordState.None || recordStateLiveData.value == RecordState.VideoSchedule ){
            mServiceManager!!.startRecord()
            notificationTitle= this.resources.getString(R.string.video_record_notification_title)
            videoPreviewVideoWindow= PreviewVideoWindow(this, object :PreviewVideoWindow.RecordAction{
                override fun onRecording(recordTime: Long, isComplete: Boolean) {
                    if (recordStateLiveData.value == RecordState.VideoRecording) {
                        listener?.onUpdateTime("", 0L, recordTime)
                        notificationContent = VideoRecordUtils.generateRecordTime(recordTime)
                        val notification= mRecordNotificationManager.getNotification(notificationTitle, notificationContent)
                        mRecordNotificationManager.notifyNewStatus(notification)
                    }
                }

                override fun onFinishRecord() {
                    listener?.onStopped()
                    notificationTitle =
                        this@RecordService.resources.getString(R.string.video_record_complete_prefix)
                    VideoRecordUtils.checkScheduleWhenRecordStop(this@RecordService)
                    val notification= mRecordNotificationManager.getNotification(notificationTitle, notificationContent)
                    mRecordNotificationManager.notifyNewStatus(notification)
                    stopForeground(true)
                }
            })
            videoPreviewVideoWindow!!.setupVideoConfiguration()
            videoPreviewVideoWindow!!.open()
            videoPreviewVideoWindow!!.startRecording()
            recordStateLiveData.value= RecordState.VideoRecording
        }
    }

    fun stopRecordVideo(){
        if(recordStateLiveData.value== RecordState.VideoRecording){
            recordStateLiveData.value= RecordState.None
            videoPreviewVideoWindow?.close()
            videoPreviewVideoWindow=null
            VideoRecordUtils.checkScheduleWhenRecordStop(this@RecordService)
            listener?.onStopped()
            mServiceManager!!.stop()
        }
    }

    fun setVideoSchedule(scheduleTime:Long){
        if(recordStateLiveData.value==RecordState.None){
            time= scheduleTime
            recordStateLiveData.value= RecordState.VideoSchedule
            notificationTitle= this.resources.getString(R.string.video_record_schedule_notify_title)
            notificationContent= this.resources.getString(R.string.video_record_schedule_video_title).plus(VideoRecordUtils.generateRecordTime(time))
            mServiceManager!!.startSchedule(time)
        }
    }

    fun cancelVideoSchedule(){
        if(recordStateLiveData.value== RecordState.VideoSchedule){
            recordStateLiveData.value=RecordState.None
            time=0L
            mServiceManager!!.stop()
        }
    }

    fun updatePreviewVideoParams(visibility:Boolean){

    }

    fun startRecordAudio(audioModel: AudioModel) {
        mAudioModel = Gson().fromJson(
            SharedPreferenceUtils.getInstance(this)?.getAudioConfig(),
            AudioModel::class.java
        )
        if (!isRecording()) {
            val mp3Name = String.format(
                Configurations.TEMPLATE_AUDIO_FILE_NAME,
                DateTimeUtils.getFullDate(Date().time)
            )
            mSoundRecorder =
                SoundRecorder(
                    this,
                    mp3Name,
                    audioModel,
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
                        }
                        SoundRecorder.MSG_ERROR_GET_MIN_BUFFER_SIZE, SoundRecorder.MSG_ERROR_CREATE_FILE, SoundRecorder.MSG_ERROR_REC_START, SoundRecorder.MSG_ERROR_AUDIO_RECORD, SoundRecorder.MSG_ERROR_AUDIO_ENCODE, SoundRecorder.MSG_ERROR_WRITE_FILE, SoundRecorder.MSG_ERROR_CLOSE_FILE -> {
                            recordAudioFailed()
                        }
                    }
                }
            })
            time = 0
            recordStateLiveData.value= RecordState.AudioRecording
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
        return recordStateLiveData.value== RecordState.VideoRecording || recordStateLiveData.value== RecordState.AudioRecording
    }

    fun getRecordState(): MutableLiveData<RecordState> {
        return recordStateLiveData
    }

    private fun stopAudioRecordByTime(time: Long) {
        if (mAudioModel!!.duration != 0L) {
            if (time == mAudioModel!!.duration) {
                stopRecording()
            }
        }
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
        unregisterReceiver(scheduleReceiver)
        unregisterReceiver(batteryLevelReceiver)
        Log.d("abcVideo", "Service killed")
    }

    enum class RecordState {
        None, AudioRecording, VideoRecording, AudioSchedule, VideoSchedule
    }

}