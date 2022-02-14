package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.os.Handler
import android.os.Looper
import com.zlw.main.recorderlib.RecordManager
import com.zlw.main.recorderlib.recorder.RecordConfig
import com.zlw.main.recorderlib.recorder.RecordHelper

class AudioRecordUtils(val listener: Listener) {

    private var isRecording: Boolean = false
    private var isPaused: Boolean = false
    private var updateTime: Long = 0
    var durationMills: Long = 0
    private var recordManager = RecordManager.getInstance()
    private var handler = Handler(Looper.getMainLooper())

    fun onStartRecording(path: String, currentConfig: RecordConfig) {
        if (isRecording) {
            durationMills += System.currentTimeMillis() - updateTime
            pauseRecordingTimer()
            RecordHelper.getInstance().pause()
            isPaused = true
            isRecording = false
        } else {
            if (isPaused) {
                updateTime = System.currentTimeMillis()
                RecordHelper.getInstance().resume()
                scheduleRecordingTimeUpdate()
            } else {
                updateTime = System.currentTimeMillis()
                RecordHelper.getInstance().start(path, currentConfig)
                scheduleRecordingTimeUpdate()
            }
            isPaused = false
            isRecording = true
        }
    }

    fun onStopRecording() {
        if (isRecording || isPaused) {
            stopRecordingTimer()
            RecordHelper.getInstance().stop()
            isRecording = false
            isPaused = false
        }
    }

    fun onPauseRecording() {
        if (isRecording) {
            RecordHelper.getInstance().pause()
            isRecording = false
            isPaused = true
        }
    }

    fun onResumeRecording() {
        if (isPaused) {
            RecordHelper.getInstance().resume()
            isRecording = true
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

    interface Listener {
        fun updateTimer(time: Long)
    }

}