package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import java.io.File
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordUtils {

    private var recorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var isPaused: Boolean = false
    private var updateTime: Long = 0
    private var durationMills: Long = 0
    private var recordFile: File? = null
    var recorderContract: RecorderContract? = null

    fun onStartRecording(audioModel: AudioModel) {
        recorder = MediaRecorder()
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD.hh.mm.ss")
        val date = simpleDateFormat.format(Date())
        val outputFile = Environment.getExternalStorageDirectory().absolutePath + "/bg_audio_$date"
        recorder!!.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(AudioMode.obtainMode(audioModel.mode))
            setAudioSamplingRate(AudioQuality.obtainQuality(audioModel.quality).toInt())
            setMaxDuration(audioModel.duration) //Duration unlimited use RECORD_MAX_DURATION or -1
            setOutputFile(outputFile)
        }
        try {
            recorder!!.prepare()
            recorder!!.start()
            updateTime = System.currentTimeMillis()
            isRecording = true
            scheduleRecordingTimeUpdate()
            recorderContract?.onStartRecord(audioModel)
            isPaused = false
        } catch (e: Exception) {

        }

    }

    fun onResumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isPaused) {
            try {
                recorder!!.resume()
                updateTime = System.currentTimeMillis()
                scheduleRecordingTimeUpdate()
                if (recorderContract != null) {
                    recorderContract!!.onResumeRecord()
                }
                isPaused = false
            } catch (e: IllegalStateException) {

            }
        }
    }

    fun onPauseRecording() {
        if (isRecording) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!isPaused) {
                    try {
                        recorder!!.pause()
                        durationMills += System.currentTimeMillis() - updateTime
                        pauseRecordingTimer()
                        if (recorderContract != null) {
                            recorderContract!!.onPauseRecord()
                        }
                        isPaused = true
                    } catch (e: IllegalStateException) {

                    }
                }
            } else {
                onStopRecording()
            }
        }
    }

    fun onStopRecording() {
        if (isRecording) {
            stopRecordingTimer()
            try {
                recorder!!.stop()
            } catch (e: RuntimeException) {

            }
            recorder!!.release()
            if (recorderContract != null) {
                recorderContract!!.onStopRecord(recordFile)
            }
            durationMills = 0
            recordFile = null
            isRecording = false
            isPaused = false
            recorder = null
        } else {

        }
    }

    fun isRecording() = isRecording

    fun isPaused() = isPaused

    private fun stopRecordingTimer() {
        updateTime = 0
    }

    private fun pauseRecordingTimer() {
        updateTime = 0
    }

    private fun scheduleRecordingTimeUpdate() {

    }

}