package com.hola360.backgroundvideorecoder.ui.setting

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutSettingVideoBinding
import com.hola360.backgroundvideorecoder.generated.callback.OnClickListener
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import kotlinx.android.synthetic.main.fragment_setting.*

class VideoSetting:BaseRecordVideoFragment<LayoutSettingVideoBinding>(), View.OnClickListener{

    override val layoutId: Int
        get() = R.layout.layout_setting_video

    override fun initView() {
        binding!!.configuration= videoConfiguration
        binding!!.camera.setOnClickListener(this)
        binding!!.rotateVideo.setOnClickListener(this)
        binding!!.videoQuality.setOnClickListener(this)
        binding!!.zoomScale.setOnClickListener(this)
        binding!!.recordDuration.setOnClickListener(this)
        binding!!.intervalTime.setOnClickListener(this)
        binding!!.previewMode.setOnClickListener(this)
        binding!!.flash.setOnClickListener(this)
        binding!!.sound.setOnClickListener(this)
        binding!!.previewSwitch.isEnabled = false
        binding!!.flashSwitch.isEnabled = false
        binding!!.soundSwitch.isEnabled = false
    }

    override fun updateSwitchThumb() {

    }

    override fun initViewModel() {

    }

    override fun applyNewVideoConfiguration() {

    }

    override fun generateCancelDialogMessages(): String {
        return ""
    }

    override fun onCancelSchedule() {

    }

    override fun startAction() {
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(p0: View?) {
        when(p0?.id){
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