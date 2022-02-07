package com.hola360.backgroundvideorecoder.ui.record.video

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment

class RecordVideo : BaseRecordVideoFragment<LayoutRecordVideoBinding>(), View.OnClickListener {

    override val layoutId: Int = R.layout.layout_record_video

    @RequiresApi(Build.VERSION_CODES.M)
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
        binding!!.configuration= videoConfiguration!!
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
        }
    }

    companion object {
        const val START = 0
        const val PAUSE = 1
        const val RESUME = 2
        const val CLEAR = 3
    }
}