package com.hola360.backgroundvideorecoder.ui.record.video

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.service.RecordService
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils

class RecordVideo: BaseRecordPageFragment<LayoutRecordVideoBinding>() {

    override val layoutId: Int = R.layout.layout_record_video
    private val cameraCapabilities: MutableList<CameraCapability> by lazy {
        VideoRecordUtils.getCameraCapabilities(requireActivity())
    }

    override fun initView() {
        binding!!.txtRecord.setOnClickListener {
            Toast.makeText(requireContext(), "Camera number: ${cameraCapabilities.size}", Toast.LENGTH_SHORT).show()
            for(item in cameraCapabilities){
                for(pos in item.qualities.map { it.toString() }){
                    Log.d("CameraTest", pos)
                }
            }
        }
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