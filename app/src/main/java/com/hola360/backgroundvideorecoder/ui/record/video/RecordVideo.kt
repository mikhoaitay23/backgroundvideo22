package com.hola360.backgroundvideorecoder.ui.record.video

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration

class RecordVideo : BaseRecordVideoFragment<LayoutRecordVideoBinding>(), View.OnClickListener {

    override val layoutId: Int = R.layout.layout_record_video

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView() {
        generateVideoConfiguration()
        binding!!.camera.setOnClickListener(this)
        binding!!.recordDuration.setOnClickListener(this)
        binding!!.intervalTime.setOnClickListener(this)
        binding!!.previewMode.setOnClickListener(this)
        binding!!.flash.setOnClickListener(this)
        binding!!.sound.setOnClickListener(this)
    }

    private fun generateVideoConfiguration() {
        videoConfiguration = if (dataPref!!.getVideoConfiguration() != "") {
            Gson().fromJson(
                dataPref!!.getVideoConfiguration()!!,
                VideoRecordConfiguration::class.java
            )
        } else {
            VideoRecordConfiguration()
        }
        binding!!.configuration = videoConfiguration!!
    }

    override fun initViewModel() {
    }

    override fun applyNewVideoConfiguration() {
        binding!!.configuration= videoConfiguration!!
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.camera -> {
                if (!showDialog) {
                    showDialog = true
                    val position = if (videoConfiguration!!.isBack) {
                        0
                    } else {
                        1
                    }
                    cameraSelectionDialog.setSelectionPos(position)
                    cameraSelectionDialog.show(requireActivity().supportFragmentManager, "Camera")
                }
            }
            R.id.recordDuration -> {
                if (!showDialog) {
                    showDialog = true
                    recordVideoDurationDialog.setupTotalTime(videoConfiguration!!.totalTime)
                    recordVideoDurationDialog.show(
                        requireActivity().supportFragmentManager,
                        "VideoDuration"
                    )
                }
            }
            R.id.intervalTime->{
                if (!showDialog) {
                    showDialog = true
                    videoIntervalDurationDialog.setupIntervalTime(videoConfiguration!!.timePerVideo)
                    videoIntervalDurationDialog.show(
                        requireActivity().supportFragmentManager,
                        "IntervalTime"
                    )
                }
            }
            R.id.previewMode->{
                videoConfiguration!!.previewMode= !videoConfiguration!!.previewMode
                binding!!.configuration= videoConfiguration
                saveNewVideoConfiguration()
            }
            R.id.flash->{
                videoConfiguration!!.flash= !videoConfiguration!!.flash
                binding!!.configuration= videoConfiguration
                saveNewVideoConfiguration()
            }
            R.id.sound->{
                videoConfiguration!!.sound= !videoConfiguration!!.sound
                binding!!.configuration= videoConfiguration
                saveNewVideoConfiguration()
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