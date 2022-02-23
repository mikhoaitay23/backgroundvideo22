package com.hola360.backgroundvideorecoder.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.*
import androidx.camera.core.CameraControl
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.anggrayudi.storage.file.toRawFile
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.record.video.model.CustomLifeCycleOwner
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.utils.Configurations
import com.hola360.backgroundvideorecoder.utils.DateTimeUtils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import java.io.File
import java.util.*

@SuppressLint("InflateParams", "ClickableViewAccessibility")
class PreviewVideoWindow(
    val context: Context,
    private val videoOrientation: Int,
    val isInternalStorage: Boolean,
    val callback: RecordAction
) {

    private var view: View? = null
    private var windowManager: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null
    private var layoutFlag: Int = 0
    private var paramX: Int = 0
    private var paramY: Int = 0
    private var cameraControl: CameraControl? = null
    private var cameraIndex = 0
    private var qualityIndex = 0
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(context) }
    private var recordingState: VideoRecordEvent? = null
    private var customLifeCycleOwner: CustomLifeCycleOwner? = null
    private val cameraCapabilities: MutableList<CameraCapability> by lazy {
        VideoRecordUtils.getCameraCapabilities(context, customLifeCycleOwner!!)
    }
    private lateinit var videoRecordConfiguration: VideoRecordConfiguration
    private var totalTimeRecord: Long = 0
    private var newInterval = false
    private var videoFileName: String = ""
    private var tempFilePath: String? = null
    private var sdCardFileUri: String? = null

    init {
        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = layoutInflater.inflate(R.layout.layout_preview_video, null)
        view!!.setOnTouchListener { v, event ->
            event?.let {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        paramX = event.rawX.toInt()
                        paramY = event.rawY.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = it.rawX.toInt() - paramX
                        val dy = it.rawY.toInt() - paramY
                        params!!.x = params!!.x + dx
                        params!!.y = params!!.y + dy
                        windowManager!!.updateViewLayout(view!!, params)
                        paramX = event.rawX.toInt()
                        paramY = event.rawY.toInt()
                    }
                }
            }
            true
        }
        layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    fun setupVideoConfiguration() {
        videoRecordConfiguration = VideoRecordUtils.getVideoConfiguration(context)
        params = if (videoRecordConfiguration.previewMode) {
            visibleParams(layoutFlag)
        } else {
            invisibleParams(layoutFlag)
        }
        params!!.x =
            -(MainActivity.SCREEN_WIDTH + context.resources.getDimensionPixelSize(R.dimen.record_preview_height)) / 2 + 10
        params!!.y =
            (MainActivity.SCREEN_HEIGHT - context.resources.getDimensionPixelSize(R.dimen.record_preview_height)) / 2 - context.resources.getDimensionPixelSize(
                R.dimen.record_bottom_pager_height
            )
        cameraIndex = if (videoRecordConfiguration.isBack) {
            0
        } else {
            1
        }
        customLifeCycleOwner = CustomLifeCycleOwner().apply {
            doOnResume()
        }
        qualityIndex = if (videoRecordConfiguration.isBack) {
            videoRecordConfiguration.backCameraQuality.coerceAtLeast(cameraCapabilities[0].qualities.size - 3)
        } else {
            videoRecordConfiguration.frontCameraQuality.coerceAtLeast(cameraCapabilities[1].qualities.size - 3)
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
        val previewView = view?.findViewById<PreviewView>(R.id.preview)

        val preview = Preview.Builder()
            .build().apply {
                setSurfaceProvider(previewView?.surfaceProvider)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        videoCapture.targetRotation = videoOrientation

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                customLifeCycleOwner!!,
                cameraSelector,
                videoCapture,
                preview
            )
            cameraControl = camera.cameraControl
            cameraControl?.enableTorch(videoRecordConfiguration.flash)
            cameraControl?.setLinearZoom(videoRecordConfiguration.zoomScale)
        } catch (exc: Exception) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e("CameraTest", "Use case binding failed", exc)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        // configure Recorder and Start recording to the mediaStoreOutput.
        if (currentRecording != null) {
            currentRecording!!.stop()
            currentRecording = null
        }
        videoFileName = String.format(
            Configurations.TEMPLATE_VIDEO_FILE_NAME,
            DateTimeUtils.getFullDate(Date().time)
        )
        val file = if (isInternalStorage) {
            VideoRecordUtils.generateVideoOutputFile(context, videoFileName)!!.toRawFile(context)
        } else {
            File(context.cacheDir, videoFileName)
        }
        if (file != null) {
            if (!isInternalStorage) {
                if (!file.exists()) {
                    file.createNewFile()
                }
                tempFilePath = file.absolutePath
            }
            val fileOutputOptions = VideoRecordUtils.generateFileOutputOptions(file)
            currentRecording = videoCapture.output
                .prepareRecording(context, fileOutputOptions)
                .apply { if (videoRecordConfiguration.sound) withAudioEnabled() }
                .start(mainThreadExecutor, captureListener)
        } else {
            close()
            callback.onFinishRecord()
        }
    }

    private val captureListener = Consumer<VideoRecordEvent> { event ->
        when (event) {
            is VideoRecordEvent.Start -> {
                callback.onStartNewInterval()
                totalTimeRecord += videoRecordConfiguration.timePerVideo
                newInterval = true
            }
            is VideoRecordEvent.Status -> {
                callback.onRecording(totalTimeRecord + event.recordingStats.recordedDurationNanos / 1000000)
                if (videoRecordConfiguration.totalTime != 0L &&
                    totalTimeRecord + event.recordingStats.recordedDurationNanos / 1000000 > videoRecordConfiguration.totalTime
                ) {
                    stopRecording()
                    close()
                    callback.onFinishRecord()
                }
                if (event.recordingStats.recordedDurationNanos / 1000000 >= videoRecordConfiguration.timePerVideo - INTERVAL_TIME_ADJUST) {
                    if (newInterval) {
                        stopRecording()
                        if (videoRecordConfiguration.totalTime == 0L || (videoRecordConfiguration.totalTime - (totalTimeRecord + videoRecordConfiguration.timePerVideo)) > 1000) {
                            startRecording()
                        } else {
                            close()
                            callback.onFinishRecord()
                        }
                        newInterval = false
                    }
                }
            }
            is VideoRecordEvent.Finalize -> {
                Log.d("abcVideo", "Error: ${event.error}")
                Log.d("abcVideo", "Error: ${event.cause}")
            }
        }
    }

    fun stopRecording() {
        if (currentRecording == null || recordingState is VideoRecordEvent.Finalize) {
            return
        }
        currentRecording!!.stop()
        currentRecording = null
        recordingState = null
        tempFilePath?.let {
            callback.onFinishInterval(tempFilePath ?: "", videoFileName)
        }
        tempFilePath = null
    }

    fun updateLayoutParams(visibility: Boolean) {
        params = if (visibility) {
            visibleParams(layoutFlag)
        } else {
            invisibleParams(layoutFlag)
        }
        windowManager!!.updateViewLayout(view!!, params!!)
    }

    private fun visibleParams(layoutFlag: Int): WindowManager.LayoutParams {
        val newParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
        }
        newParams.x =
            -(MainActivity.SCREEN_WIDTH + context.resources.getDimensionPixelSize(R.dimen.record_preview_height)) / 2 + 10
        newParams.y =
            (MainActivity.SCREEN_HEIGHT - context.resources.getDimensionPixelSize(R.dimen.record_preview_height)) / 2 - context.resources.getDimensionPixelSize(
                R.dimen.record_bottom_pager_height
            )
        return newParams
    }

    private fun invisibleParams(layoutFlag: Int): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            1, 1,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    fun open() {
        try {
            // check if the view is already
            // inflated or present in the window
            if (view?.windowToken == null) {
                if (view?.parent == null) {
                    windowManager?.addView(view, params!!)
                    startRecording()
                }
            }
        } catch (e: Exception) {
            Log.d("Error", e.toString())
        }
    }

    fun close() {
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

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        const val INTERVAL_TIME_ADJUST = 200L
    }

    interface RecordAction {
        fun onStartNewInterval()

        fun onRecording(recordTime: Long)

        fun onFinishInterval(filePath: String, videoFileName: String)

        fun onFinishRecord()
    }

}
