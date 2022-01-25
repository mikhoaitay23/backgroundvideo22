package com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.video.MediaStoreOutputOptions
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.databinding.FragmentBottomSheetRecordAudioBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.PathUtils
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import com.hola360.backgroundvideorecoder.utils.Utils.isAndroidQ
import java.io.File
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordBottomSheetFragment :
    BaseBottomSheetDialog<FragmentBottomSheetRecordAudioBinding>(), View.OnClickListener {

    private lateinit var viewModel: AudioRecordBottomSheetViewModel
    private var audioRecordUtils: AudioRecordUtils? = null
    private var audioModel: AudioModel? = null
    private var recorder: MediaRecorder? = null
    private var isPaused = false
    private var isRecording = false
    private var updateTime: Long = 0
    private var durationMills: Long = 0
    private var recordFile: File? = null

    override fun getLayout() = R.layout.fragment_bottom_sheet_record_audio

    override fun initView() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = AudioRecordBottomSheetViewModel.Factory(requireActivity().application)
        viewModel =
            ViewModelProvider(this, factory)[AudioRecordBottomSheetViewModel::class.java]
        audioRecordUtils = AudioRecordUtils()
        audioModel = AudioModel()

        initClick()

//        audioRecordUtils!!.onStartRecording(audioModel!!)
        if (SystemUtils.hasPermissions(
                requireContext(),
                *Constants.STORAGE_PERMISSION_UNDER_STORAGE_SCOPE
            )
        ) {
            onStartRecording(requireContext(), audioModel!!)
        } else {
            resultLauncher.launch(Constants.STORAGE_PERMISSION_UNDER_STORAGE_SCOPE)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding!!.btnPause -> {
                when {
                    audioRecordUtils!!.isPaused() -> audioRecordUtils!!.onResumeRecording()
                    audioRecordUtils!!.isRecording() -> audioRecordUtils!!.onPauseRecording()
                }
            }
            binding!!.btnAbort -> {

            }
            binding!!.btnSave -> {
                onStopRecording()
            }
        }
    }

    private fun initClick() {
        binding!!.btnPause.setOnClickListener(this)
        binding!!.btnAbort.setOnClickListener(this)
        binding!!.btnSave.setOnClickListener(this)
    }

    private fun onStartRecording(context: Context, audioModel: AudioModel) {
        recorder = MediaRecorder()
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD.hh.mm.ss")
        val date = simpleDateFormat.format(Date())
        val pdfCollection = if (isAndroidQ()) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        val fileName = "/bg_audio_$date"
        val parentFolder = (Environment.DIRECTORY_DOCUMENTS).plus(File.separator)
            .plus(Constants.FOLDER_NAME)
        val now = Date()
        val contentValues = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.TITLE, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "audio/mpeg")
            put(MediaStore.Files.FileColumns.DATE_ADDED, now.time / 1000)
            put(MediaStore.Files.FileColumns.DATE_MODIFIED, now.time / 1000)
            if (isAndroidQ()) {
                put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH,
                    parentFolder
                )
            } else {
                val finalPdfFile = File(
                    Utils.getDocumentationFolder(),
                    "/bg_audio_$date"
                )
                put(MediaStore.Files.FileColumns.DATA, finalPdfFile.absolutePath.toString())
            }

        }
        try {
            val uri = requireContext().contentResolver.insert(pdfCollection, contentValues)
            if (uri != null) {
                Log.d("TAG", "onStartRecording: ${PathUtils.getPath(requireContext(), uri)}")
                recorder!!.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioChannels(AudioMode.obtainMode(audioModel.mode))
                    setAudioSamplingRate(AudioQuality.obtainQuality(audioModel.quality).toInt())
                    setMaxDuration(audioModel.duration) //Duration unlimited use RECORD_MAX_DURATION or -1
                    setOutputFile(PathUtils.getPath(requireContext(), uri))
                }
                try {
                    recorder!!.prepare()
                    recorder!!.start()
                    updateTime = System.currentTimeMillis()
                    isRecording = true
//                scheduleRecordingTimeUpdate()
                    isPaused = false
                } catch (e: Exception) {

                }
            } else {
                Log.d("TAG", "onStartRecording: ")
            }
        } catch (e: Exception) {
            Log.d("TAG", "onStartRecording: ${e.message}")
        }

    }

    private fun onStopRecording() {
        if (isRecording) {
            stopRecordingTimer()
            try {
                recorder!!.stop()
            } catch (e: RuntimeException) {

            }
            recorder!!.release()
            durationMills = 0
            recordFile = null
            isRecording = false
            isPaused = false
            recorder = null
        } else {

        }
    }

    fun isRecording() = isRecording

    fun isPaused() = isPaused

    private fun stopRecordingTimer() {
        updateTime = 0
    }

    private fun pauseRecordingTimer() {
        updateTime = 0
    }

    private fun scheduleRecordingTimeUpdate() {

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String?, Boolean?>? ->
            if (SystemUtils.hasPermissions(
                    requireContext(),
                    *Constants.STORAGE_PERMISSION_UNDER_STORAGE_SCOPE
                )
            ) {
                onStartRecording(requireContext(), audioModel!!)
            } else {
                SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
            }
        }

}