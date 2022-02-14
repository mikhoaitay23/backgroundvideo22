package com.hola360.backgroundvideorecoder.ui.setting

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutSettingVideoBinding
import com.hola360.backgroundvideorecoder.generated.callback.OnClickListener
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import kotlinx.android.synthetic.main.fragment_setting.*

class VideoSetting:BaseRecordVideoFragment<LayoutSettingVideoBinding>(), View.OnClickListener{

    override val layoutId: Int
        get() = R.layout.layout_setting_video
    private val cameraCapabilities: MutableList<CameraCapability> by lazy {
        VideoRecordUtils.getCameraCapabilities(requireContext(), this)
    }
    private val frontCameraQuality: MutableList<String> by lazy {
        VideoRecordUtils.getCameraQuality(false, cameraCapabilities)
    }
    private val frontCameraQualityDialog:ListSelectionBotDialog by lazy {
        val title= getString(R.string.video_record_configuration_quality)
        ListSelectionBotDialog(title, frontCameraQuality, object : ListSelectionAdapter.OnItemListSelection {
            override fun onSelection(position: Int) {
                videoConfiguration!!.frontCameraQuality=position
                setCameraQualityTextView()
                saveNewVideoConfiguration()
                applyNewVideoConfiguration()
                frontCameraQualityDialog.dialog?.dismiss()
            }
        }, dismissCallback)
    }
    private val backCameraQuality:MutableList<String> by lazy {
        VideoRecordUtils.getCameraQuality(true, cameraCapabilities)
    }
    private val backCameraQualityDialog:ListSelectionBotDialog by lazy {
        val title= getString(R.string.video_record_configuration_quality)
        ListSelectionBotDialog(title, backCameraQuality, object : ListSelectionAdapter.OnItemListSelection {
            override fun onSelection(position: Int) {
                videoConfiguration!!.backCameraQuality=position
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

    private fun setCameraQualityTextView(){
        binding!!.txtVideoQuality.text= if(videoConfiguration!!.isBack){
            backCameraQuality[videoConfiguration!!.backCameraQuality]
        }else{
            frontCameraQuality[videoConfiguration!!.frontCameraQuality]
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
        binding!!.previewSwitch.isEnabled = false
        binding!!.flashSwitch.isEnabled = false
        binding!!.soundSwitch.isEnabled = false
    }

    override fun updateSwitchThumb() {

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
    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.camera -> {
                onCameraFacingSelect()
            }
            R.id.videoQuality->{
                if (!showDialog) {
                    showDialog = true
                    val dialog= if(videoConfiguration!!.isBack){
                        backCameraQualityDialog.setSelectionPos(videoConfiguration!!.backCameraQuality)
                        backCameraQualityDialog
                    }else{
                        frontCameraQualityDialog.setSelectionPos(videoConfiguration!!.frontCameraQuality)
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