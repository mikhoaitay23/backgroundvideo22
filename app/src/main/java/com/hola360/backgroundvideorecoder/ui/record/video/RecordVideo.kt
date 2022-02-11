package com.hola360.backgroundvideorecoder.ui.record.video

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils

class RecordVideo : BaseRecordVideoFragment<LayoutRecordVideoBinding>(), View.OnClickListener{

    override val layoutId: Int = R.layout.layout_record_video

    override fun initView() {
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
//        setSwitchThumb()
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
        binding!!.isRecording= (requireActivity() as MainActivity).recordStatus== MainActivity.RECORD_VIDEO
        if((requireActivity() as MainActivity).recordStatus != MainActivity.RECORD_VIDEO){
            binding!!.recordTime.text = resources.getString(R.string.video_record_time_zero)
        }
        if((requireActivity() as MainActivity).recordService == null){
            (requireActivity() as MainActivity).bindService()
        }
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

    fun updateRecordingTime(time:Long){
        if(binding?.isRecording==false){
            binding?.isRecording=true
        }
        binding?.recordTime?.text= VideoRecordUtils.generateRecordTime(time)
    }

    fun onRecordCompleted(){
        binding?.isRecording=false
        binding?.recordTime?.text= resources.getString(R.string.video_record_time_zero)
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
                startRecordOrSetSchedule()
            }
        }
    }

    override fun startAction() {
        if(!binding!!.isRecording){
            (requireActivity() as MainActivity).handleRecordVideoIntent(MainActivity.RECORD_VIDEO)
        }else{
            (requireActivity() as MainActivity).handleRecordVideoIntent(MainActivity.STOP_VIDEO_RECORD)
            (requireActivity() as MainActivity).recordStatus= MainActivity.NO_RECORDING
            binding!!.recordTime.text = resources.getString(R.string.video_record_time_zero)
        }
        binding!!.isRecording= !binding!!.isRecording
    }


}