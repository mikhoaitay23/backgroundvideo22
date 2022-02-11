package com.hola360.backgroundvideorecoder.ui.record.video

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
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

class ScheduleVideo : BaseRecordVideoFragment<LayoutScheduleVideoBinding>(), View.OnClickListener {

    override val layoutId: Int = R.layout.layout_schedule_video
    private var calendar: Calendar = Calendar.getInstance()
    private val confirmCancelSchedule: ConfirmDialog by lazy {
        ConfirmDialog(object : ConfirmDialog.OnConfirmOke {
            override fun onConfirm() {
                cancelSchedule()
            }
        }, dismissCallback)
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
        setSwitchThumb()
    }

    private fun setSwitchThumb() {
        val thumbRes = if (binding!!.schedule) {
            R.drawable.bg_switch_thumb_un_clickable
        } else {
            R.drawable.bg_switch_thumb
        }
        binding!!.flashSwitch.setThumbResource(thumbRes)
        binding!!.soundSwitch.setThumbResource(thumbRes)
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
    private fun saveSchedule() {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setScheduleBroadcast(time:Long){
//        (requireActivity() as MainActivity).handleRecordVideoIntent(MainActivity.SCHEDULE_RECORD_VIDEO)
        VideoRecordUtils.setAlarmSchedule(requireContext(), time)
    }

    private fun cancelSchedule() {
        binding!!.schedule = false
        setSwitchThumb()
        calendar.timeInMillis = System.currentTimeMillis().also {
            binding!!.scheduleTime=it
        }
        dataPref!!.putSchedule("")
//        (requireActivity() as MainActivity).handleRecordVideoIntent(MainActivity.CANCEL_SCHEDULE_RECORD_VIDEO)
        VideoRecordUtils.cancelAlarmSchedule(requireContext())
    }

    fun checkScheduleWhenRecordStop(){
        val videoSchedule= VideoRecordUtils.getVideoSchedule(requireContext())
        binding?.schedule= videoSchedule.scheduleTime>System.currentTimeMillis()
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
                if (!showDialog) {
                    showDialog = true
                    val messages =
                        if (recordSchedule!!.scheduleTime != 0L && recordSchedule!!.scheduleTime < System.currentTimeMillis()
                            && (recordSchedule!!.scheduleTime + videoConfiguration!!.totalTime) > System.currentTimeMillis()
                        ) {
                            resources.getString(R.string.video_record_schedule_cancel_progress_message)
                        } else {
                            resources.getString(R.string.video_record_schedule_cancel_message)
                        }
                    confirmCancelSchedule.setMessages(messages)
                    confirmCancelSchedule.show(requireActivity().supportFragmentManager, "Confirm")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun startAction() {
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            Utils.showInvalidateTime(binding!!.root)
        } else {
            binding!!.schedule = true
            setSwitchThumb()
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

    companion object{
        const val BROADCAST_INTENT_REQUEST_CODE=1
    }

}