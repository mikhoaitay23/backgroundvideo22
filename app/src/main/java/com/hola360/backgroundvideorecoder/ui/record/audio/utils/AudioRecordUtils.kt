package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.content.ContentValues
import android.content.Context
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.video.MediaStoreOutputOptions
import androidx.core.content.ContentProviderCompat.requireContext
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.zlw.main.recorderlib.RecordManager
import com.zlw.main.recorderlib.recorder.RecordConfig
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordUtils {

    private var isRecording: Boolean = false
    private var isPaused: Boolean = false
    private var updateTime: Long = 0
    private var durationMills: Long = 0
    private var recordManager = RecordManager.getInstance()
    private var handler = Handler(Looper.getMainLooper())

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
        if (isRecording) {
            stopRecordingTimer()
            recordManager.stop()
            isRecording = false
            isPaused = false
        }
    }

    fun scheduleRecordingTimeUpdate() {
        handler.postDelayed({
            if (recordManager != null) {
                try {
                    val curTime = System.currentTimeMillis()
                    durationMills += curTime - updateTime
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

}