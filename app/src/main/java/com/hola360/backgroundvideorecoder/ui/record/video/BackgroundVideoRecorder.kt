package com.hola360.backgroundvideorecoder.ui.record.video

import android.content.Context
import androidx.camera.core.CameraControl
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.record.video.model.CustomLifeCycleOwner
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils

class BackgroundVideoRecorde(val context: Context) {

    private var videoConfiguration: VideoRecordConfiguration?= null
    private var generalSetting: SettingGeneralModel?= null
    private var cameraControl: CameraControl?=null
    private var cameraIndex = 0
    private var qualityIndex = 0
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(context) }
    private var recordingState: VideoRecordEvent?=null
    private var customLifeCycleOwner: CustomLifeCycleOwner?=null
    private val cameraCapabilities: MutableList<CameraCapability> by lazy {
        VideoRecordUtils.getCameraCapabilities(context, customLifeCycleOwner!!)
    }
    private var totalTimeRecord:Long= 0
    private var newInterval=false

    init {

    }

    fun applyNewConfiguration(){
        videoConfiguration= VideoRecordUtils.getVideoConfiguration(context)
        generalSetting= VideoRecordUtils.getSettingGeneralModel(context)
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val cameraSelector = cameraCapabilities[cameraIndex].camSelector
        val quality: Quality = cameraCapabilities[cameraIndex].qualities[qualityIndex]
        val qualitySelector = QualitySelector.from(quality)
    }
}