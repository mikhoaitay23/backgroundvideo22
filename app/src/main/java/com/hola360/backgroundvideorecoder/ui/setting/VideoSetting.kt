package com.hola360.backgroundvideorecoder.ui.setting

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutSettingVideoBinding
import com.hola360.backgroundvideorecoder.ui.dialog.VideoZoomDialog
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils

class VideoSetting:BaseRecordVideoFragment<LayoutSettingVideoBinding>(), View.OnClickListener{

    override val layoutId: Int
        get() = R.layout.layout_setting_video
    private val cameraCapabilities: MutableList<CameraCapability> by lazy {
        VideoRecordUtils.getCameraCapabilities(requireContext(), this)
    }
    private val frontCameraQuality: MutableList<String> by lazy {
        generateCameraQualityList(VideoRecordUtils.getCameraQuality(false, cameraCapabilities), false)
    }
    private val frontCameraDiff:Int by lazy {
        if(VideoRecordUtils.getCameraQuality(false, cameraCapabilities).size > frontCameraQuality.size){
            VideoRecordUtils.getCameraQuality(false, cameraCapabilities).size - frontCameraQuality.size
        }else{
            0
        }
    }
    private val frontCameraQualityDialog:ListSelectionBotDialog by lazy {
        val title= getString(R.string.video_record_configuration_quality)
        ListSelectionBotDialog(title, frontCameraQuality, object : ListSelectionAdapter.OnItemListSelection {
            override fun onSelection(position: Int) {
                videoConfiguration!!.frontCameraQuality= if(frontCameraDiff>0){
                    position+ frontCameraDiff
                }else{
                    position
                }
                setCameraQualityTextView()
                saveNewVideoConfiguration()
                applyNewVideoConfiguration()
                frontCameraQualityDialog.dialog?.dismiss()
            }
        }, dismissCallback)
    }
    private val backCameraQuality:MutableList<String> by lazy {
        generateCameraQualityList(VideoRecordUtils.getCameraQuality(true, cameraCapabilities), true)
    }
    private val backCameraDiff:Int by lazy {
        if(VideoRecordUtils.getCameraQuality(true, cameraCapabilities).size > backCameraQuality.size){
            VideoRecordUtils.getCameraQuality(true, cameraCapabilities).size - backCameraQuality.size
        }else{
            0
        }
    }
    private val backCameraQualityDialog:ListSelectionBotDialog by lazy {
        val title= getString(R.string.video_record_configuration_quality)
        ListSelectionBotDialog(title, backCameraQuality, object : ListSelectionAdapter.OnItemListSelection {
            override fun onSelection(position: Int) {
                videoConfiguration!!.backCameraQuality=if(backCameraDiff>0){
                    position+ backCameraDiff
                }else{
                    position
                }
                setCameraQualityTextView()
                saveNewVideoConfiguration()
                applyNewVideoConfiguration()
                backCameraQualityDialog.dialog?.dismiss()
            }
        }, dismissCallback)
    }
    private val cameraRotationDialog: ListSelectionBotDialog by lazy {
        val title = resources.getString(R.string.video_record_configuration_rotation)
        val itemList = resources.getStringArray(R.array.camera_orientation).toMutableList()
        ListSelectionBotDialog(title, itemList, object : ListSelectionAdapter.OnItemListSelection {
            override fun onSelection(position: Int) {
                videoConfiguration!!.videoOrientation = position
                saveNewVideoConfiguration()
                applyNewVideoConfiguration()
                cameraRotationDialog.dialog?.dismiss()
            }
        }, dismissCallback)
    }
    private val zoomScaleDialog: VideoZoomDialog by lazy {
        VideoZoomDialog(object : VideoZoomDialog.OnSelectZoomScale{
            override fun onSelectZoomScale(scale:Float) {
                videoConfiguration!!.zoomScale= scale
                saveNewVideoConfiguration()
                applyNewVideoConfiguration()
            }
        }, dismissCallback)
    }

    private fun generateCameraQualityList(data:MutableList<String>, isBack:Boolean):MutableList<String>{
        val newQualityList= mutableListOf<String>()
        return if(data.size>3){
            newQualityList.add(data[data.size-3])
            newQualityList.add(data[data.size-2])
            newQualityList.add(data[data.size-1])
            if(isBack){
                if(videoConfiguration!!.backCameraQuality< data.size-3){
                    videoConfiguration!!.backCameraQuality= data.size-3
                    applyNewVideoConfiguration()
                    saveNewVideoConfiguration()
                }
            }else{
                if(videoConfiguration!!.frontCameraQuality<data.size-3){
                    videoConfiguration!!.frontCameraQuality= data.size-3
                    applyNewVideoConfiguration()
                    saveNewVideoConfiguration()
                }
            }
            newQualityList
        }else{
            newQualityList.apply {
                addAll(data)
            }
        }
    }

    private fun setCameraQualityTextView(){
        binding!!.txtVideoQuality.text= if(videoConfiguration!!.isBack){
            backCameraQuality[(videoConfiguration!!.backCameraQuality- backCameraDiff).coerceAtLeast(0)]
        }else{
            frontCameraQuality[(videoConfiguration!!.frontCameraQuality- frontCameraDiff).coerceAtLeast(0)]
        }
    }

    override fun initView() {
        binding!!.configuration= videoConfiguration
        binding!!.camera.setOnClickListener(this)
        binding!!.rotateVideo.setOnClickListener(this)
        binding!!.videoQuality.setOnClickListener(this)
        setCameraQualityTextView()
        binding!!.zoomScale.setOnClickListener(this)
        binding!!.recordDuration.setOnClickListener(this)
        binding!!.intervalTime.setOnClickListener(this)
        binding!!.previewMode.setOnClickListener(this)
        binding!!.flash.setOnClickListener(this)
        binding!!.sound.setOnClickListener(this)
    }

    override fun initViewModel() {
    }

    override fun applyNewVideoConfiguration() {
        binding!!.configuration= videoConfiguration
        setCameraQualityTextView()
    }

    override fun generateCancelDialogMessages(): String {
        return ""
    }

    override fun onCancelSchedule() {
    }

    override fun startAction() {
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        checkPreviewMode()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.camera -> {
                onCameraFacingSelect()
            }
            R.id.videoQuality->{
                if (!showDialog) {
                    showDialog = true
                    val dialog= if(videoConfiguration!!.isBack){
                        backCameraQualityDialog.setSelectionPos((videoConfiguration!!.backCameraQuality- backCameraDiff).coerceAtLeast(0))
                        backCameraQualityDialog
                    }else{
                        frontCameraQualityDialog.setSelectionPos((videoConfiguration!!.frontCameraQuality- frontCameraDiff).coerceAtLeast(0))
                        frontCameraQualityDialog
                    }
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        "VideoQuality"
                    )
                }
            }
            R.id.rotateVideo->{
                if (!showDialog) {
                    showDialog = true
                    cameraRotationDialog.setSelectionPos(videoConfiguration!!.videoOrientation)
                    cameraRotationDialog.show(
                        requireActivity().supportFragmentManager,
                        "VideoRotation"
                    )
                }
            }
            R.id.zoomScale->{
                if (!showDialog) {
                    showDialog = true
                    zoomScaleDialog.setupZoomValue(videoConfiguration!!.zoomScale)
                    zoomScaleDialog.show(
                        requireActivity().supportFragmentManager,
                        "ZoomScale"
                    )
                }
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