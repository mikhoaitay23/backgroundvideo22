package com.hola360.backgroundvideorecoder.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import com.hola360.backgroundvideorecoder.R
import java.lang.Exception

import android.content.Context.WINDOW_SERVICE
import android.view.*
import androidx.camera.core.CameraControl
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.record.video.model.CustomLifeCycleOwner
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils

@SuppressLint("InflateParams", "ClickableViewAccessibility")
class PreviewVideoWindow(val context: Context, val callback:RecordAction) {

    private var view: View?= null
    private var windowManager: WindowManager?= null
    private var params: WindowManager.LayoutParams?=null
    private var layoutFlag:Int=0
    private var paramX:Int =0
    private var paramY:Int =0
    private var cameraControl:CameraControl?=null
    private var cameraIndex = 0
    private var qualityIndex = 0
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(context) }
    private var recordingState:VideoRecordEvent?=null
    private var customLifeCycleOwner: CustomLifeCycleOwner?=null
    private val cameraCapabilities: MutableList<CameraCapability> by lazy {
        VideoRecordUtils.getCameraCapabilities(context, customLifeCycleOwner!!)
    }
    private lateinit var videoRecordConfiguration: VideoRecordConfiguration
    private var totalTimeRecord:Long= 0
    private var newInterval=false

    init {
        windowManager= context.getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutInflater= context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view= layoutInflater.inflate(R.layout.layout_preview_video, null)
        view!!.setOnTouchListener { v, event ->
            event?.let {
                if (it.action == MotionEvent.ACTION_DOWN) {
                    paramX = it.x.toInt()
                    paramY = it.y.toInt()
                }
                if (it.action == MotionEvent.ACTION_MOVE) {
                    val dx = event.x - paramX
                    val dy = event.y - paramY
                    params!!.x= params!!.x +dx.toInt()
                    params!!.y= params!!.y+dy.toInt()
                    windowManager!!.updateViewLayout(view!!, params)
                    paramX = it.x.toInt()
                    paramY = it.y.toInt()
                }
            }
            true
        }
        layoutFlag =if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    fun setupVideoConfiguration(){
        videoRecordConfiguration= VideoRecordUtils.getVideoConfiguration(context)
        params= if(videoRecordConfiguration.previewMode){
            visibleParams(layoutFlag)
        }else{
            invisibleParams(layoutFlag)
        }
        params!!.x= -(MainActivity.SCREEN_WIDTH +context.resources.getDimensionPixelSize(R.dimen.record_preview_height))/2+10
        params!!.y= (MainActivity.SCREEN_HEIGHT - context.resources.getDimensionPixelSize(R.dimen.record_preview_height))/2 - context.resources.getDimensionPixelSize(R.dimen.record_bottom_pager_height)
        cameraIndex= if(videoRecordConfiguration.isBack){
            0
        }else{
            1
        }
        qualityIndex= if(videoRecordConfiguration.isBack){
            videoRecordConfiguration.backCameraQuality
        }else{
            videoRecordConfiguration.frontCameraQuality
        }
        customLifeCycleOwner= CustomLifeCycleOwner().apply {
            doOnResume()
        }
        totalTimeRecord = -videoRecordConfiguration.timePerVideo
        bindCaptureUserCase()
    }

    @SuppressLint("RestrictedApi")
    private fun bindCaptureUserCase() {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val cameraSelector = cameraCapabilities[cameraIndex].camSelector
        val quality: Quality = cameraCapabilities[cameraIndex].qualities[qualityIndex]
        val qualitySelector = QualitySelector.from(quality)
        val previewView= view?.findViewById<PreviewView>(R.id.preview)

        val preview = Preview.Builder()
            .build().apply {
                setSurfaceProvider(previewView?.surfaceProvider)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        videoCapture.targetRotation= Surface.ROTATION_90

        try {
            cameraProvider.unbindAll()
            val camera= cameraProvider.bindToLifecycle(
                customLifeCycleOwner!!,
                cameraSelector,
                videoCapture,
                preview
            )
            cameraControl= camera.cameraControl
            cameraControl?.enableTorch(videoRecordConfiguration.flash)
            cameraControl?.setLinearZoom(videoRecordConfiguration.zoomScale)
        } catch (exc: Exception) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e("CameraTest", "Use case binding failed", exc)
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        // configure Recorder and Start recording to the mediaStoreOutput.
        if(currentRecording!= null){
            currentRecording!!.stop()
            currentRecording = null
        }
        Log.d("abcVideo", "Start record")
        val mediaStoreOutput = VideoRecordUtils.generateMediaStoreOutput(context)
//        val file= File(context.cacheDir, "Record_video_${System.currentTimeMillis()}.mp4")
//        val fileOutputOptions= VideoRecordUtils.generateFileOutput(file)

        currentRecording = videoCapture.output
            .prepareRecording(context, mediaStoreOutput)
            .apply { if (videoRecordConfiguration.sound) withAudioEnabled() }
            .start(mainThreadExecutor, captureListener)
    }

    private val captureListener = Consumer<VideoRecordEvent> { event ->
        when(event){
            is VideoRecordEvent.Start->{
                totalTimeRecord+= videoRecordConfiguration.timePerVideo
                newInterval=true
            }
            is VideoRecordEvent.Status->{
                callback.onRecording(totalTimeRecord+ event.recordingStats.recordedDurationNanos/1000000,
                    totalTimeRecord+ event.recordingStats.recordedDurationNanos/1000000>= videoRecordConfiguration.totalTime)
                if(videoRecordConfiguration.totalTime!= 0L &&
                    totalTimeRecord+ event.recordingStats.recordedDurationNanos/1000000> videoRecordConfiguration.totalTime){
                    close()
                    callback.onFinishRecord()
                }
                if(event.recordingStats.recordedDurationNanos/1000000>= videoRecordConfiguration.timePerVideo-INTERVAL_TIME_ADJUST){
                    if(newInterval){
                        stopRecording()
                        startRecording()
                        newInterval=false
                    }
                }
            }
            is VideoRecordEvent.Finalize->{
                Log.d("abcVideo", "Error ${event.error}  ${event.cause}")
            }
        }
    }

    private fun stopRecording(){
        if (currentRecording == null || recordingState is VideoRecordEvent.Finalize) {
            return
        }
        val recording = currentRecording
        if (recording != null) {
            recording.stop()
            currentRecording = null
            recordingState= null
        }
    }

    fun updateLayoutParams(visibility:Boolean){
        params= if(visibility){
             visibleParams(layoutFlag)
        }else{
            invisibleParams(layoutFlag)
        }
        windowManager!!.updateViewLayout(view!!, params!!)
    }

    private fun visibleParams(layoutFlag:Int):WindowManager.LayoutParams{
        return WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)
    }

    private fun invisibleParams(layoutFlag:Int):WindowManager.LayoutParams{
        return WindowManager.LayoutParams(1, 1,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)
    }

    fun open(){
        try {
            // check if the view is already
            // inflated or present in the window
            if (view?.windowToken == null) {
                if (view?.parent == null) {
                    windowManager?.addView(view, params!!)
                }
            }
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }

    fun close(){
        try {
            // remove the view from the window
            customLifeCycleOwner!!.doOnDestroy()
            (context.getSystemService(WINDOW_SERVICE) as WindowManager).removeView(view!!)
            // invalidate the view
            view?.invalidate()
            // remove all views
            (view?.parent as ViewGroup).removeAllViews()

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }

    companion object{
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val INTERVAL_TIME_ADJUST=200L
    }

    interface RecordAction{
        fun onRecording(time:Long, isComplete:Boolean)

        fun onFinishRecord()
    }

}
