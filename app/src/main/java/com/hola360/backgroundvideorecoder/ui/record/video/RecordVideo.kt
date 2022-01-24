package com.hola360.backgroundvideorecoder.ui.record.video

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.service.RecordService

class RecordVideo : BaseRecordPageFragment<LayoutRecordVideoBinding>() {

    override val layoutId: Int = R.layout.layout_record_video

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView() {
        binding!!.start.setOnClickListener {
            if (binding!!.isRecording){
                runRecordService(CLEAR)
            }else{
                runRecordService(START)
            }
            binding!!.isRecording= !binding!!.isRecording
        }

    }

    fun runRecordService(status: Int) {
        val intent = Intent(requireContext(), RecordService::class.java)
        intent.putExtra("Record_video", status)
        requireContext().startService(intent)
    }

    fun stopService() {
//        val intent = Intent(requireContext(), RecordService::class.java)
//        requireContext().stopService(intent)
    }

    override fun initViewModel() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object{
        const val START= 0
        const val STOP=1
        const val RESUME=2
        const val CLEAR=3
    }
}