package com.hola360.backgroundvideorecoder.ui.record.video

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration

class RecordVideo : BaseRecordPageFragment<LayoutRecordVideoBinding>() {

    override val layoutId: Int = R.layout.layout_record_video
    private var videoConfiguration: VideoRecordConfiguration?= null
    private var startRecord=false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView() {
        generateVideoConfiguration()
    }

    private fun generateVideoConfiguration(){
        videoConfiguration=if(dataPref!!.getVideoConfiguration() != ""){
            Gson().fromJson(dataPref!!.getVideoConfiguration()!!, VideoRecordConfiguration::class.java)
        }else{
            VideoRecordConfiguration()
        }
        binding!!.configuration= videoConfiguration!!
    }

    fun runRecordService(status: Int) {
        val intent = Intent(requireContext(), RecordService::class.java)
        intent.putExtra("Video_status", status)
        intent.putExtra("Video_configuration", fakeVideoConfiguration())
        requireContext().startService(intent)
    }

    override fun initViewModel() {

    }

   private fun fakeVideoConfiguration():VideoRecordConfiguration{
       return VideoRecordConfiguration().apply {
           totalTime= 18000L
           timePerVideo=5000L
           previewMode= true
       }
   }

    companion object{
        const val START= 0
        const val PAUSE=1
        const val RESUME=2
        const val CLEAR=3
    }
}