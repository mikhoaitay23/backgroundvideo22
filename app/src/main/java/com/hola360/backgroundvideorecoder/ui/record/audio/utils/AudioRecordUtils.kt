package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.media.AudioFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.zlw.main.recorderlib.RecordManager
import com.zlw.main.recorderlib.recorder.RecordConfig

class AudioRecordUtils {

    private var isRecording: Boolean = false
    private var isPaused: Boolean = false
    private var updateTime: Long = 0
    var durationMills: Long = 0
    private var recordManager = RecordManager.getInstance()
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var listener: Listener

    fun onStartRecording(audioModel: AudioModel) {
        recordManager.changeFormat(RecordConfig.RecordFormat.MP3)
        recordManager.changeRecordConfig(
            recordManager.recordConfig.setSampleRate(
                AudioQuality.obtainQuality(audioModel.quality).toInt()
            )
        )
        recordManager.changeRecordConfig(
            recordManager.recordConfig.setEncodingConfig(
                AudioFormat.ENCODING_PCM_16BIT
            )
        )
        recordManager.changeRecordConfig(
            recordManager.recordConfig.setChannelConfig(AudioMode.obtainMode(audioModel.mode))
        )
        if (isRecording) {
            durationMills += System.currentTimeMillis() - updateTime
            pauseRecordingTimer()
            recordManager.pause()
            isPaused = true
            isRecording = false
        } else {
            if (isPaused) {
                updateTime = System.currentTimeMillis()
                recordManager.resume()
                scheduleRecordingTimeUpdate()
            } else {
                updateTime = System.currentTimeMillis()
                recordManager.start()
                scheduleRecordingTimeUpdate()
            }
            isPaused = false
            isRecording = true
        }
    }

    fun onStopRecording() {
        if (isRecording || isPaused) {
            stopRecordingTimer()
            recordManager.stop()
            isRecording = false
            isPaused = false
        }
    }

    private fun scheduleRecordingTimeUpdate() {
        handler.postDelayed({
            if (recordManager != null) {
                try {
                    val curTime = System.currentTimeMillis()
                    durationMills += curTime - updateTime
                    listener.updateTimer(durationMills)
                    updateTime = curTime
                } catch (e: IllegalStateException) {

                }
                scheduleRecordingTimeUpdate()
            }
        }, 1000)
    }

    fun isRecording() = isRecording

    fun isPaused() = isPaused

    private fun stopRecordingTimer() {
        handler.removeCallbacksAndMessages(null)
        updateTime = 0
    }

    private fun pauseRecordingTimer() {
        handler.removeCallbacksAndMessages(null)
        updateTime = 0
    }

    fun registerListener(listener: Listener){
        this.listener = listener
    }

    interface Listener {
        fun updateTimer(time: Long)
    }

    companion object {
        const val START = 0
        const val PAUSE = 1
        const val RESUME = 2
        const val STOP = 3
    }

}