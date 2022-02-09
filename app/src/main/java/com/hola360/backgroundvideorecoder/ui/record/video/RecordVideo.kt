package com.hola360.backgroundvideorecoder.ui.record.video

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils

class RecordVideo : BaseRecordVideoFragment<LayoutRecordVideoBinding>(), View.OnClickListener {

    override val layoutId: Int = R.layout.layout_record_video

    override fun initView() {
        binding!!.isRecording = isRecording
        applyNewVideoConfiguration()
        binding!!.camera.setOnClickListener(this)
        binding!!.recordDuration.setOnClickListener(this)
        binding!!.intervalTime.setOnClickListener(this)
        binding!!.previewMode.setOnClickListener(this)
        binding!!.flash.setOnClickListener(this)
        binding!!.sound.setOnClickListener(this)
        binding!!.previewSwitch.isEnabled = false
        binding!!.flashSwitch.isEnabled = false
        binding!!.soundSwitch.isEnabled = false
        binding!!.start.setOnClickListener(this)
        setSwitchThumb()
    }

    private fun setSwitchThumb() {
        val thumbRes = if (isRecording) {
            R.drawable.bg_switch_thumb_un_clickable
        } else {
            R.drawable.bg_switch_thumb
        }
        binding!!.previewSwitch.setThumbResource(thumbRes)
        binding!!.flashSwitch.setThumbResource(thumbRes)
        binding!!.soundSwitch.setThumbResource(thumbRes)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onPreviewModeChange() {
        videoConfiguration!!.previewMode = !videoConfiguration!!.previewMode
        applyNewVideoConfiguration()
        saveNewVideoConfiguration()
        if (videoConfiguration!!.previewMode) {
            if (!Settings.canDrawOverlays(requireContext())) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireContext().packageName)
                )
                startActivityForResult(intent, 0)
            }
        }
    }

    override fun initViewModel() {
    }

    override fun applyNewVideoConfiguration() {
        binding!!.configuration = videoConfiguration!!
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        checkPreviewMode()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPreviewMode(){
        if (videoConfiguration!!.previewMode) {
            if (!Settings.canDrawOverlays(requireContext())) {
                videoConfiguration!!.previewMode = false
                applyNewVideoConfiguration()
                saveNewVideoConfiguration()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.camera -> {
                onCameraFacingSelect()
            }
            R.id.recordDuration -> {
                onVideoRecordDurationSelect()
            }
            R.id.intervalTime->{
                onVideoIntervalSelect()
            }
            R.id.previewMode->{
                onPreviewModeChange()
            }
            R.id.flash->{
                onFlashModeChange()
            }
            R.id.sound->{
                onSoundModeChange()
            }
            R.id.start->{
                if(!binding!!.isRecording){
                    if(SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)){
                        startRecordVideo()
                    }else{
                        getCameraPermission.launch(Constants.CAMERA_RECORD_PERMISSION)
                    }
                }
            }
        }
    }

    private fun startRecordVideo(){
    }

    private val getCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String?, Boolean?>? ->
        if (SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)) {
            startRecordVideo()
        } else {
            SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
        }
    }

    companion object {
        const val START = 0
        const val PAUSE = 1
        const val RESUME = 2
        const val CLEAR = 3
    }
}