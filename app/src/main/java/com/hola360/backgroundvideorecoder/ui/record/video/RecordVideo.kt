package com.hola360.backgroundvideorecoder.ui.record.video

import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoBinding
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.dialog.ConfirmDialog
import com.hola360.backgroundvideorecoder.ui.dialog.RecordAlertDialog
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import java.lang.IllegalStateException

class RecordVideo : BaseRecordVideoFragment<LayoutRecordVideoBinding>(), View.OnClickListener, RecordService.Listener {

    override val layoutId: Int = R.layout.layout_record_video
    private var orientationAngle= 0
    private val orientationListener: OrientationEventListener by lazy {
        object : OrientationEventListener(requireContext()){
            override fun onOrientationChanged(orientation: Int) {
                orientationAngle= orientation
            }
        }
    }
    private var showBatteryAlertDialog=false
    private var showStorageAlertDialog=false
    private val batteryAlertDialog: RecordAlertDialog by lazy {
        RecordAlertDialog(object : ConfirmDialog.OnConfirmOke{
            override fun onConfirm() {
                if((requireActivity() as MainActivity).recordService!!.getRecordState().value == RecordService.RecordState.VideoRecording){
                    (requireActivity() as MainActivity).recordService!!.stopRecordVideo()
                }else{
                    (requireActivity() as MainActivity).recordService!!.stopRecording()
                }
            }
        })
    }
    private val generalSetting: SettingGeneralModel by lazy {
        VideoRecordUtils.getSettingGeneralModel(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).recordService!!.registerListener(this)
        (requireActivity() as MainActivity).recordService!!.getRecordState().observe(this) {
            when (it) {
                RecordService.RecordState.VideoRecording -> {
                    binding!!.isRecording = true
                }
                else -> {
                    binding!!.isRecording = false
                    binding!!.recordTime.text = getString(R.string.video_record_time_zero)
                }
            }
        }
        orientationListener.enable()
    }

    override fun initView() {
        applyNewVideoConfiguration()
        binding!!.camera.setOnClickListener(this)
        binding!!.recordDuration.setOnClickListener(this)
        binding!!.intervalTime.setOnClickListener(this)
        binding!!.previewMode.setOnClickListener(this)
        binding!!.flash.setOnClickListener(this)
        binding!!.sound.setOnClickListener(this)
        binding!!.previewSwitch.isEnabled = false
        binding!!.flashSwitch.isEnabled = false
        binding!!.soundSwitch.isEnabled = false
        binding!!.start.setOnClickListener(this)
    }

    override fun updateSwitchThumb() {
        binding!!.previewSwitch.setThumbResource(switchThumb)
        binding!!.flashSwitch.setThumbResource(switchThumb)
        binding!!.soundSwitch.setThumbResource(switchThumb)
    }

    override fun initViewModel() {
    }

    override fun applyNewVideoConfiguration() {
        binding!!.configuration = videoConfiguration!!
    }

    override fun onResume() {
        super.onResume()
        checkPreviewMode()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.camera -> {
                onCameraFacingSelect()
            }
            R.id.recordDuration -> {
                onVideoRecordDurationSelect()
            }
            R.id.intervalTime -> {
                onVideoIntervalSelect()
            }
            R.id.previewMode -> {
                onPreviewModeChange()
            }
            R.id.flash -> {
                onFlashModeChange()
            }
            R.id.sound -> {
                onSoundModeChange()
            }
            R.id.start -> {
                startRecordOrSetSchedule()
            }
        }
    }

    override fun onUpdateTime(fileName: String, duration: Long, curTime: Long) {
        if (!binding!!.isRecording && curTime > 0) {
            binding!!.isRecording = true
        }
        binding!!.recordTime.text = VideoRecordUtils.generateRecordTime(curTime)
        checkBatteryPercent()
        checkStoragePercent()
    }

    override fun onByteBuffer(buf: ShortArray?, minBufferSize: Int) {

    }

    override fun onStopped() {
        showBatteryAlertDialog=false
        showStorageAlertDialog=false
    }

    private fun checkBatteryPercent(){
        if(context != null){
            if(generalSetting.checkBattery && !showBatteryAlertDialog){
                val curBatteryPercent= SystemUtils.getBatteryPercent(requireContext())
                if(curBatteryPercent<= BATTERY_PERCENT_ALERT){
                    showBatteryAlertDialog=true
                    batteryAlertDialog.isBattery= true
                    batteryAlertDialog.show((requireActivity() as MainActivity).supportFragmentManager, ALERT_TAG)
                }
            }
        }
    }

    private fun checkStoragePercent(){
        if(context!= null){
            if(generalSetting.checkStorage && !showStorageAlertDialog){
                val percent= SystemUtils.checkStoragePercent(requireContext(), generalSetting.storageId)
                if(percent>= STORAGE_PERCENT_ALERT){
                    showStorageAlertDialog=true
                    batteryAlertDialog.isBattery= false
                    batteryAlertDialog.show((requireActivity() as MainActivity).supportFragmentManager, ALERT_TAG)
                }
            }
        }
    }

    override fun startAction() {
        if (!binding!!.isRecording) {
            if (recordSchedule!!.scheduleTime > 0L && System.currentTimeMillis() + videoConfiguration!!.totalTime > recordSchedule!!.scheduleTime) {
                showCancelDialog()
            } else {
                (requireActivity() as MainActivity).recordService!!.startRecordVideo(VideoRecordUtils.getVideoRotation(requireContext(), orientationAngle))
            }
        } else {
            (requireActivity() as MainActivity).recordService!!.stopRecordVideo()
            binding!!.recordTime.text = getString(R.string.video_record_time_zero)
        }
    }

    override fun generateCancelDialogMessages(): String {
        return getString(R.string.video_record_schedule_cancel_message)
    }

    override fun onCancelSchedule() {
        cancelSchedule()
    }

    override fun onDestroy() {
        super.onDestroy()
        orientationListener.disable()
    }

    companion object{
        const val BATTERY_PERCENT_ALERT= 10
        const val STORAGE_PERCENT_ALERT= 0.9
        const val ALERT_TAG= "Alert_dialog"
    }
}