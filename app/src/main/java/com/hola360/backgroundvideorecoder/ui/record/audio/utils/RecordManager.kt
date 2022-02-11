package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.app.Application
import com.hola360.backgroundvideorecoder.service.RecordService
import com.zlw.main.recorderlib.recorder.RecordConfig
import com.zlw.main.recorderlib.recorder.listener.*
import com.zlw.main.recorderlib.utils.Logger

class RecordManager {

    private var context: Application? = null

    fun init(application: Application?, showLog: Boolean) {
        context = application
        Logger.IsDebug = showLog
    }

    fun start() {
        if (context == null) {
            return
        }
        RecordService().startRecording("")
    }

    fun stop() {
        if (context == null) {
            return
        }
        RecordService().stopRecording()
    }

    fun resume() {
        if (context == null) {
            return
        }
        RecordService().resumeRecording()
    }

    fun pause() {
        if (context == null) {
            return
        }
        RecordService().pauseRecording()
    }

    fun setRecordStateListener(listener: RecordStateListener?) {
        RecordService().setRecordStateListener(listener)
    }

    fun setRecordDataListener(listener: RecordDataListener?) {
        RecordService().setRecordDataListener(listener)
    }

    fun setRecordFftDataListener(recordFftDataListener: RecordFftDataListener?) {
        RecordService().setRecordFftDataListener(recordFftDataListener)
    }

    fun setRecordResultListener(listener: RecordResultListener?) {
        RecordService().setRecordResultListener(listener)
    }

    fun setRecordSoundSizeListener(listener: RecordSoundSizeListener?) {
        RecordService().setRecordSoundSizeListener(listener)
    }


    fun changeFormat(recordFormat: RecordConfig.RecordFormat?): Boolean {
        return RecordService().changeFormat(recordFormat)
    }


    fun changeRecordConfig(recordConfig: RecordConfig?): Boolean {
        return RecordService().changeRecordConfig(recordConfig)
    }

    fun getRecordConfig(): RecordConfig? {
        return RecordService().getRecordConfig()
    }

    fun changeRecordDir(recordDir: String?) {
        RecordService().changeRecordDir(recordDir)
    }

    fun getState(): RecordHelper.RecordState? {
        return RecordService().getState()
    }
}