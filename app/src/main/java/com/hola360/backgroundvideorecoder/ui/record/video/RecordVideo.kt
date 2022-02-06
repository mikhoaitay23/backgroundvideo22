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
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration

class RecordVideo : BaseRecordPageFragment<LayoutRecordVideoBinding>(), View.OnClickListener {

    override val layoutId: Int = R.layout.layout_record_video
    private var videoConfiguration: VideoRecordConfiguration?= null
    private val cameraSelectionDialog:ListSelectionBotDialog by lazy {
        val title= resources.getString(R.string.video_record_configuration_camera)
        val itemList= resources.getStringArray(R.array.camera_facing).toMutableList()
        ListSelectionBotDialog(title, itemList, object : ListSelectionAdapter.OnItemListSelection{
            override fun onSelection(position:Int) {
                videoConfiguration!!.isBack= position==0
                binding!!.configuration= videoConfiguration
                cameraSelectionDialog.dialog?.dismiss()
                showDialog=false
            }
        })
    }
    private var showDialog= false
    private var startRecord=false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView() {
        generateVideoConfiguration()
        binding!!.camera.setOnClickListener(this)
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

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.camera->{
                if(!showDialog){
                    showDialog=true
                    val position= if(videoConfiguration!!.isBack){
                        0
                    }else{
                        1
                    }
                    cameraSelectionDialog.setSelectionPos(position)
                    cameraSelectionDialog.show(requireActivity().supportFragmentManager, "Camera")
                }
            }
        }
    }

    companion object{
        const val START= 0
        const val PAUSE=1
        const val RESUME=2
        const val CLEAR=3
    }
}