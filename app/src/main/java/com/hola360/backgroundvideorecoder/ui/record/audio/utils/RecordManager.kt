package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.annotation.SuppressLint
import android.app.Application
import com.hola360.backgroundvideorecoder.service.RecordService
import com.zlw.main.recorderlib.RecordManager
import com.zlw.main.recorderlib.recorder.RecordConfig
import com.zlw.main.recorderlib.recorder.RecordConfig.RecordFormat
import com.zlw.main.recorderlib.recorder.RecordHelper.RecordState
import com.zlw.main.recorderlib.recorder.listener.*
import com.zlw.main.recorderlib.utils.Logger

class RecordManager {

    private val TAG = RecordManager::class.java.simpleName

    @SuppressLint("StaticFieldLeak")
    private var context: Application? = null

    /**
     * 初始化
     *
     * @param application Application
     * @param showLog     是否开启日志
     */
    fun init(application: Application?, showLog: Boolean) {
        context = application
        Logger.IsDebug = showLog
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


    fun changeFormat(recordFormat: RecordFormat?): Boolean {
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

    fun getState(): RecordState? {
        return RecordService().getState()
    }
}