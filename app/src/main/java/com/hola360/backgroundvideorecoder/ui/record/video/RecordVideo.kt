package com.hola360.backgroundvideorecoder.ui.record.video

import android.content.Intent
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.service.RecordService

class RecordVideo: BaseRecordPageFragment<LayoutRecordVideoBinding>() {

    override val layoutId: Int = R.layout.layout_record_video

    override fun initView() {
        binding!!.start.setOnClickListener {
            startRecordService()
        }
        binding!!.stop.setOnClickListener {
            stopService()
        }
    }

    private fun startRecordService(){
        val intent= Intent(requireContext(), RecordService::class.java)
        intent.putExtra("Key", "Video")
        requireContext().startService(intent)
    }

    private fun stopService(){
        val intent= Intent(requireContext(), RecordService::class.java)
        requireContext().stopService(intent)
    }

    override fun initViewModel() {

    }
}