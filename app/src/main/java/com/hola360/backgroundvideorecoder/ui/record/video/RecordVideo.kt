package com.hola360.backgroundvideorecoder.ui.record.video

import android.os.Bundle
import android.util.Log
import android.view.View
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils

class RecordVideo : BaseRecordVideoFragment<LayoutRecordVideoBinding>(), View.OnClickListener{

    override val layoutId: Int = R.layout.layout_record_video

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).recordStatusLiveData.observe(this) {
            when (it.status) {
                MainActivity.RECORD_VIDEO -> {
                    if (!binding!!.isRecording && it.time > 0) {
                        Log.d("abcVideo", "Recording: ")
                        binding!!.isRecording = true
                    }
                    binding!!.recordTime.text = VideoRecordUtils.generateRecordTime(it.time)
                }
                MainActivity.NO_RECORDING -> {
                    binding!!.isRecording = false
                    binding!!.recordTime.text = getString(R.string.video_record_time_zero)
                }
            }
        }
    }

    override fun initView() {
        binding!!.isRecording= isRecording
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
    }

    override fun updateSwitchThumb() {
        binding!!.previewSwitch.setThumbResource(switchThumb)
        binding!!.flashSwitch.setThumbResource(switchThumb)
        binding!!.soundSwitch.setThumbResource(switchThumb)
    }

    override fun initViewModel() {
    }

    override fun applyNewVideoConfiguration() {
        binding!!.configuration = videoConfiguration!!
    }

    override fun onResume() {
        super.onResume()
        checkPreviewMode()
    }

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
            if(recordSchedule!!.scheduleTime>0L && System.currentTimeMillis()+ videoConfiguration!!.totalTime> recordSchedule!!.scheduleTime){
                showCancelDialog()
            }else{
                Log.d("abcVideo", "Start Recording: ")
                (requireActivity() as MainActivity).handleRecordStatus(MainActivity.RECORD_VIDEO)
            }
        }else{
            (requireActivity() as MainActivity).handleRecordStatus(MainActivity.STOP_VIDEO_RECORD)
            (requireActivity() as MainActivity).onRecordCompleted()
        }
    }

    override fun generateCancelDialogMessages(): String {
        return getString(R.string.video_record_schedule_cancel_message)
    }

    override fun onCancelSchedule() {
        cancelSchedule()
    }

}