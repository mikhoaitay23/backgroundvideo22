package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import java.io.File

interface RecorderContract {

    interface RecorderCallback {
        fun onStartRecord(output: File?)
        fun onPauseRecord()
        fun onResumeRecord()
        fun onRecordProgress(mills: Long, amp: Int)
        fun onStopRecord(output: File?)
        fun onError(throwable: Exception?)
    }

    interface Recorder {
        fun setRecorderCallback(callback: RecorderCallback?)
        fun startRecording(audioModel: AudioModel)
        fun resumeRecording()
        fun pauseRecording()
        fun stopRecording()
        fun isRecording(): Boolean
        fun isPaused(): Boolean
    }
}