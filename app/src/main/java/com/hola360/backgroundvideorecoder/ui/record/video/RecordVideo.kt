package com.hola360.backgroundvideorecoder.ui.record.video

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.QualitySelector
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.hola360.backgroundvideorecoder.R
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
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
    private val windowPreview:PreviewVideoWindow by lazy {
        PreviewVideoWindow(requireContext(), cameraCapabilities, this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView() {
        binding!!.txtRecord.setOnClickListener {
            windowPreview.open()
        }
        binding!!.start.setOnClickListener {

        }
        binding!!.stop.setOnClickListener {

        }
    }

    private fun startRecordService() {
        val intent = Intent(requireContext(), RecordService::class.java)
        intent.putExtra("Key", "Video")
        requireContext().startService(intent)
    }

    private fun stopService() {
//        val intent = Intent(requireContext(), RecordService::class.java)
//        requireContext().stopService(intent)
    }

    override fun initViewModel() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}