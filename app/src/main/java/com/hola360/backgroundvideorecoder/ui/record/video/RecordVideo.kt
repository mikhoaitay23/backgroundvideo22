package com.hola360.backgroundvideorecoder.ui.record.video

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.QualitySelector
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.hola360.backgroundvideorecoder.R
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.service.RecordService
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils.getAspectRatio
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils.getAspectRatioString
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RecordVideo : BaseRecordPageFragment<LayoutRecordVideoBinding>() {

    override val layoutId: Int = R.layout.layout_record_video
    private val cameraCapabilities: MutableList<CameraCapability> by lazy {
        VideoRecordUtils.getCameraCapabilities(requireActivity())
    }
    private var cameraIndex = 0
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(requireContext()) }
    private var qualityIndex = 0
    private var audioEnabled = false
    private lateinit var recordingState:VideoRecordEvent

    override fun initView() {
        binding!!.txtRecord.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Camera number: ${cameraCapabilities.size}",
                Toast.LENGTH_SHORT
            ).show()
            for (item in cameraCapabilities) {
                for (pos in item.qualities.map { it.toString() }) {
                    Log.d("CameraTest", pos)
                }
            }
        }
        binding!!.start.setOnClickListener {
            startRecording()
        }
        binding!!.stop.setOnClickListener {
            stopRecording()
        }
    }

    private fun startRecordService() {
        val intent = Intent(requireContext(), RecordService::class.java)
        intent.putExtra("Key", "Video")
        requireContext().startService(intent)
    }

    private fun stopService() {
        val intent = Intent(requireContext(), RecordService::class.java)
        requireContext().stopService(intent)
    }

    override fun initViewModel() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindCaptureUserCase()
    }

    private fun bindCaptureUserCase() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext()).get()
        val cameraSelector = cameraCapabilities[cameraIndex].camSelector
        val quality:Quality = cameraCapabilities[cameraIndex].qualities[qualityIndex]
        val qualitySelector = QualitySelector.from(quality)
        binding!!.preview.updateLayoutParams<ConstraintLayout.LayoutParams> {
            val orientation = this@RecordVideo.resources.configuration.orientation
            dimensionRatio = quality.getAspectRatioString(quality, orientation == Configuration.ORIENTATION_PORTRAIT)
        }

        val preview = Preview.Builder()
            .setTargetAspectRatio(quality.getAspectRatio(quality))
            .build().apply {
                setSurfaceProvider(binding!!.preview.surfaceProvider)
            }

        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
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
    private fun startRecording() {
        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        val name = "CameraX-recording-" +
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            requireActivity().contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        // configure Recorder and Start recording to the mediaStoreOutput.
        if(currentRecording!= null){
            currentRecording!!.stop()
            currentRecording = null
        }
        currentRecording = videoCapture.output
            .prepareRecording(requireActivity(), mediaStoreOutput)
            .apply { if (audioEnabled) withAudioEnabled() }
            .start(mainThreadExecutor, captureListener)

    }

    private val captureListener = Consumer<VideoRecordEvent> { event ->
        // cache the recording state
        if (event !is VideoRecordEvent.Status)
            recordingState = event

        if (event is VideoRecordEvent.Finalize) {
            // display the captured video
            Toast.makeText(requireContext(), "Finish", Toast.LENGTH_SHORT).show()
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



    companion object{
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

}