package com.hola360.backgroundvideorecoder.ui.record.video

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import java.util.*

class RecordVideo : BaseRecordVideoFragment<LayoutRecordVideoBinding>(), View.OnClickListener{

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
        binding!!.isRecording= (requireActivity() as MainActivity).recordStatus== MainActivity.VIDEO_RECORD
        if((requireActivity() as MainActivity).recordStatus != MainActivity.VIDEO_RECORD){
            binding!!.recordTime.text = resources.getString(R.string.video_record_time_zero)
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
        binding?.isRecording=true
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
                if(SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)){
                    startRecordVideo()
                }else{
                    getCameraPermission.launch(Constants.CAMERA_RECORD_PERMISSION)
                }
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun startRecordVideo(){
        if(!binding!!.isRecording){
            (requireActivity() as MainActivity).startRecordVideo(START)
//            val alarmManager= requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            val intent= Intent(requireContext(), RecordService::class.java)
//            intent.putExtra("Video_status", START)
//            val pendingIntent= PendingIntent.getService(requireContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            val calendar= Calendar.getInstance()
//            calendar.add(Calendar.SECOND, 5)
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }else{
            (requireActivity() as MainActivity).startRecordVideo(CLEAR)
            binding!!.recordTime.text = resources.getString(R.string.video_record_time_zero)
        }
        binding!!.isRecording= !binding!!.isRecording
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
        const val INTERVAL = 1
        const val CLEAR = 3
    }
}