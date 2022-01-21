package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import java.io.File

interface RecorderContract {

        fun onStartRecord(audioModel: AudioModel)
        fun onPauseRecord()
        fun onResumeRecord()
        fun onRecordProgress(mills: Long, amp: Int)
        fun onStopRecord(output: File?)
        fun onError(throwable: Exception?)
}