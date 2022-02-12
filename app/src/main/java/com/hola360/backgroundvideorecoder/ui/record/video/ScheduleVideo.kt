package com.hola360.backgroundvideorecoder.ui.record.video

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutScheduleVideoBinding
import com.hola360.backgroundvideorecoder.ui.dialog.ConfirmDialog
import com.hola360.backgroundvideorecoder.ui.record.RecordSchedule
import com.hola360.backgroundvideorecoder.ui.record.video.base.BaseRecordVideoFragment
import com.hola360.backgroundvideorecoder.utils.Utils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import java.util.*

class ScheduleVideo(val recordVideoFragment:VideoRecordFragment) : BaseRecordVideoFragment<LayoutScheduleVideoBinding>(), View.OnClickListener {

    override val layoutId: Int = R.layout.layout_schedule_video
    private var calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).recordStatusLiveData.observe(this, {
            when(it.status){
                MainActivity.RECORD_VIDEO->{
                    if(!binding!!.isRecording && it.time>0){
                        binding!!.isRecording = true
                    }
                }
                MainActivity.NO_RECORDING->{
                    binding!!.isRecording=false
                    checkScheduleWhenRecordStop()
                }
            }
        })
    }

    private fun checkScheduleWhenRecordStop(){
        if(binding!!.schedule && recordSchedule!!.scheduleTime<System.currentTimeMillis()){
            binding!!.schedule=false
            recordSchedule= RecordSchedule()
            dataPref!!.putSchedule("")
        }
    }

    override fun initView() {
        if (recordSchedule!!.scheduleTime == 0L) {
            binding!!.schedule = false
            binding!!.scheduleTime = System.currentTimeMillis()
        } else {
            binding!!.schedule = true
            binding!!.scheduleTime = recordSchedule!!.scheduleTime
            binding!!.scheduleCard.schedule = recordSchedule
        }
        applyNewVideoConfiguration()
        binding!!.date.setOnClickListener(this)
        binding!!.time.setOnClickListener(this)
        binding!!.camera.setOnClickListener(this)
        binding!!.recordDuration.setOnClickListener(this)
        binding!!.intervalTime.setOnClickListener(this)
        binding!!.flash.setOnClickListener(this)
        binding!!.sound.setOnClickListener(this)
        binding!!.setSchedule.setOnClickListener(this)
        binding!!.scheduleCard.cancelSchedule.setOnClickListener(this)
        binding!!.flashSwitch.isEnabled = false
        binding!!.soundSwitch.isEnabled = false
    }

    override fun updateSwitchThumb() {
        binding!!.flashSwitch.setThumbResource(switchThumb)
        binding!!.soundSwitch.setThumbResource(switchThumb)
    }

    override fun initViewModel() {
    }

    override fun applyNewVideoConfiguration() {
        binding!!.configuration = videoConfiguration
    }

    private fun selectDate() {
        val curDay = calendar.get(Calendar.DAY_OF_MONTH)
        val curMonth = calendar.get(Calendar.MONTH)
        val curYear = calendar.get(Calendar.YEAR)
        val datePicker = DatePickerDialog(requireContext(), R.style.TimeAndDatePickerStyle,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                if (calendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                    Utils.showInvalidateTime(binding!!.root)
                } else {
                    binding!!.scheduleTime = calendar.timeInMillis
                }
            }, curYear, curMonth, curDay
        )
        datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
        datePicker.show()
    }

    private fun selectTime() {
        val curHour = calendar.get(Calendar.HOUR_OF_DAY)
        val curMinute = calendar.get(Calendar.MINUTE)
        val timePicker = TimePickerDialog(requireContext(), R.style.TimeAndDatePickerStyle,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                if (calendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                    Utils.showInvalidateTime(binding!!.root)
                } else {
                    binding!!.scheduleTime = calendar.timeInMillis
                }
            }, curHour, curMinute, false
        )
        timePicker.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.date -> {
                selectDate()
            }
            R.id.time -> {
                selectTime()
            }
            R.id.camera -> {
                onCameraFacingSelect()
            }
            R.id.recordDuration -> {
                onVideoRecordDurationSelect()
            }
            R.id.intervalTime -> {
                onVideoIntervalSelect()
            }
            R.id.flash -> {
                onFlashModeChange()
            }
            R.id.sound -> {
                onSoundModeChange()
            }
            R.id.setSchedule -> {
                startRecordOrSetSchedule()
            }
            R.id.cancelSchedule -> {
                showCancelDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun startAction() {
        if(!binding!!.isRecording){
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                    Utils.showInvalidateTime(binding!!.root)
            } else {
                binding!!.schedule = true
                recordSchedule = RecordSchedule().apply {
                    isVideo = true
                    scheduleTime = calendar.timeInMillis
                }
                binding!!.scheduleCard.schedule = recordSchedule
                val scheduleValue = Gson().toJson(recordSchedule)
                dataPref!!.putSchedule(scheduleValue)
                setScheduleBroadcast(calendar.timeInMillis)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setScheduleBroadcast(time:Long){
        (requireActivity() as MainActivity).handleRecordStatus(MainActivity.SCHEDULE_RECORD_VIDEO)
        VideoRecordUtils.setAlarmSchedule(requireContext(), time)
    }

    override fun generateCancelDialogMessages(): String {
            return if (recordSchedule!!.scheduleTime != 0L && binding!!.isRecording) {
                resources.getString(R.string.video_record_schedule_cancel_progress_message)
            } else {
                resources.getString(R.string.video_record_schedule_cancel_message)
            }
    }

    override fun onCancelSchedule() {
        if(!binding!!.isRecording){
            binding!!.schedule = false
            calendar.timeInMillis = System.currentTimeMillis().also {
                binding!!.scheduleTime=it
            }
            cancelSchedule()
        }else{
            (requireActivity() as MainActivity).handleRecordStatus(MainActivity.STOP_VIDEO_RECORD)
            (requireActivity() as MainActivity).onRecordCompleted()
        }

    }

    companion object{
        const val BROADCAST_INTENT_REQUEST_CODE=1
    }

}