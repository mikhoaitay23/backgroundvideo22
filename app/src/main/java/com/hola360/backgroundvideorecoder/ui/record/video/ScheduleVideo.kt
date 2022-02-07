package com.hola360.backgroundvideorecoder.ui.record.video

import android.view.View
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutScheduleVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment

class ScheduleVideo: BaseRecordVideoFragment<LayoutScheduleVideoBinding>(), View.OnClickListener {

    override val layoutId: Int = R.layout.layout_schedule_video

    override fun initView() {
        generateVideoConfiguration()
        applyNewVideoConfiguration()
        binding!!.camera.setOnClickListener(this)
        binding!!.recordDuration.setOnClickListener(this)
        binding!!.intervalTime.setOnClickListener(this)
        binding!!.previewMode.setOnClickListener(this)
        binding!!.flash.setOnClickListener(this)
        binding!!.sound.setOnClickListener(this)
        binding!!.previewSwitch.isEnabled=false
        binding!!.flashSwitch.isEnabled=false
        binding!!.soundSwitch.isEnabled= false
    }

    override fun initViewModel() {
    }

    override fun applyNewVideoConfiguration() {
        binding!!.configuration= videoConfiguration
    }

    override fun onClick(v: View?) {
        when (v?.id) {
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
        }
    }
}