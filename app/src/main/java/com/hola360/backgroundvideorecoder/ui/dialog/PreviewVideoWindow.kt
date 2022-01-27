package com.hola360.backgroundvideorecoder.ui.dialog

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.hola360.backgroundvideorecoder.R
import java.lang.Exception
import android.view.ViewGroup

import android.content.Context.WINDOW_SERVICE
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.record.video.model.CustomLifeCycleOwner
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils.getAspectRatio
import java.text.SimpleDateFormat
import java.util.*

class PreviewVideoWindow(val context: Context) {

    private var view: View?= null
    private var windowManager: WindowManager?= null
    private var params: WindowManager.LayoutParams?=null
    private var cameraIndex = 0
    private var qualityIndex = 0
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(context) }
    var recordingState:VideoRecordEvent?=null
    private val customLifeCycleOwner= CustomLifeCycleOwner()
    private val cameraCapabilities: MutableList<CameraCapability> by lazy {
        VideoRecordUtils.getCameraCapabilities(context, customLifeCycleOwner)
    }
    private var videoRecordConfiguration: VideoRecordConfiguration?= null
    private var totalTimeRecord:Long= 0

    init {
        windowManager= context.getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutInflater= context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view= layoutInflater.inflate(R.layout.layout_preview_video, null)
//        val start =  view?.findViewById<TextView>(R.id.start)
//       start?.setOnClickListener {
//           startRecording()
//           start.isEnabled= false
//        }
        customLifeCycleOwner.doOnResume()
    }

    fun setupVideoConfiguration(videoRecordConfiguration: VideoRecordConfiguration){
        this.videoRecordConfiguration= videoRecordConfiguration
        val layoutFlag =if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE;
        }
        params= if(videoRecordConfiguration.previewMode){
            WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        }else{
            WindowManager.LayoutParams(1, 1,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
        }
        params!!.gravity= Gravity.BOTTOM
        cameraIndex= if(videoRecordConfiguration.isBack){
            0
        }else{
            1
        }
        qualityIndex= videoRecordConfiguration.cameraQuality
    }

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

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                customLifeCycleOwner,
                cameraSelector,
                videoCapture,
                preview
            )
        } catch (exc: Exception) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e("CameraTest", "Use case binding failed", exc)
        }
    }

    @SuppressLint("MissingPermission")
    fun startRecording() {
        val mediaStoreOutput = VideoRecordUtils.generateMediaStoreOutput(context)
        // configure Recorder and Start recording to the mediaStoreOutput.
        if(currentRecording!= null){
            currentRecording!!.stop()
            currentRecording = null
        }
        currentRecording = videoCapture.output
            .prepareRecording(context, mediaStoreOutput)
            .apply { if (videoRecordConfiguration!!.sound) withAudioEnabled() }
            .start(mainThreadExecutor, captureListener)
    }

    fun pauseAndResume() {
        when (recordingState) {
            is VideoRecordEvent.Start -> {
                currentRecording?.pause()
            }
            is VideoRecordEvent.Pause -> {
                currentRecording?.resume()
            }
            is VideoRecordEvent.Resume -> {
                currentRecording?.pause()
            }
        }
    }

    private val captureListener = Consumer<VideoRecordEvent> { event ->
        // cache the recording state
        when(event){
            is VideoRecordEvent.Status->{
                Log.d("abcVideo", "New status: ${event.recordingStats.recordedDurationNanos/1000000}")
                if(totalTimeRecord+ event.recordingStats.recordedDurationNanos/1000000> videoRecordConfiguration!!.totalTime){
                    stopRecording()
                    Toast.makeText(context, "Finish", Toast.LENGTH_SHORT).show()
                    close()
                }
                if(event.recordingStats.recordedDurationNanos/1000000>= videoRecordConfiguration!!.timePerVideo){
                    startRecording()
                }
            }
            is VideoRecordEvent.Finalize->{
                if(event.recordingStats.recordedDurationNanos==0L){
                    totalTimeRecord+= videoRecordConfiguration!!.timePerVideo
                    Toast.makeText(context, "Finish", Toast.LENGTH_SHORT).show()
                }
            }
            else->{
                recordingState = event
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
        }
    }

    fun open(){
        try {
            // check if the view is already
            // inflated or present in the window
            if (view?.windowToken == null) {
                if (view?.parent == null) {
                    windowManager?.addView(view, params!!)
                    bindCaptureUserCase()
                }
            }
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }

    fun close(){
        try {
            // remove the view from the window
                    customLifeCycleOwner.doOnDestroy()
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
    }

}