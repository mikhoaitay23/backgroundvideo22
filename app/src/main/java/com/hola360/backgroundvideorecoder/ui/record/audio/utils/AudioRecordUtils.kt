package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.media.MediaRecorder
import android.os.Build
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import java.io.File
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.RuntimeException

class AudioRecordUtils : RecorderContract.Recorder {

    private var recorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var isPaused: Boolean = false
    private var updateTime: Long = 0
    private var durationMills: Long = 0
    private var recorderCallback: RecorderContract.RecorderCallback? = null
    private var recordFile: File? = null

    override fun setRecorderCallback(callback: RecorderContract.RecorderCallback?) {
        this.recorderCallback = callback
    }

    override fun startRecording(audioModel: AudioModel) {
        recorder = MediaRecorder()
        recorder!!.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(AudioMode.obtainMode(audioModel.mode))
            setAudioSamplingRate(AudioQuality.obtainQuality(audioModel.quality).toInt())
            setMaxDuration(audioModel.duration) //Duration unlimited use RECORD_MAX_DURATION or -1
//            setOutputFile()
        }
        try {
            recorder!!.prepare()
            recorder!!.start()
            updateTime = System.currentTimeMillis()
            isRecording = true
            scheduleRecordingTimeUpdate()
//            recorderCallback?.onStartRecord(recordFile)
            isPaused = false
        } catch (e: Exception) {

        }

    }

    override fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isPaused) {
            try {
                recorder!!.resume()
                updateTime = System.currentTimeMillis()
                scheduleRecordingTimeUpdate()
                if (recorderCallback != null) {
                    recorderCallback!!.onResumeRecord()
                }
                isPaused = false
            } catch (e: IllegalStateException) {

            }
        }
    }

    override fun pauseRecording() {
        if (isRecording) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!isPaused) {
                    try {
                        recorder!!.pause()
                        durationMills += System.currentTimeMillis() - updateTime
                        pauseRecordingTimer()
                        if (recorderCallback != null) {
                            recorderCallback!!.onPauseRecord()
                        }
                        isPaused = true
                    } catch (e: IllegalStateException) {

                    }
                }
            } else {
                stopRecording()
            }
        }
    }

    override fun stopRecording() {
        if (isRecording) {
            stopRecordingTimer()
            try {
                recorder!!.stop()
            } catch (e: RuntimeException) {

            }
            recorder!!.release()
            if (recorderCallback != null) {
                recorderCallback!!.onStopRecord(recordFile)
            }
            durationMills = 0
            recordFile = null
            isRecording = false
            isPaused = false
            recorder = null
        } else {

        }
    }

    override fun isRecording() = isRecording

    override fun isPaused() = isPaused

    private fun stopRecordingTimer() {
        updateTime = 0
    }

    private fun pauseRecordingTimer() {
        updateTime = 0
    }

    private fun scheduleRecordingTimeUpdate() {

    }

}