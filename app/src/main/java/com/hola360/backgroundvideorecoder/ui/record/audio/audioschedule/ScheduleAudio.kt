package com.hola360.backgroundvideorecoder.ui.record.audio.audioschedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.databinding.LayoutScheduleAudioBinding
import com.hola360.backgroundvideorecoder.ui.dialog.ConfirmDialog
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordSchedule
import com.hola360.backgroundvideorecoder.utils.Utils
import java.util.*

class ScheduleAudio : BaseRecordPageFragment<LayoutScheduleAudioBinding>(), View.OnClickListener {

    private lateinit var viewModel: ScheduleAudioViewModel
    private lateinit var mainActivity: MainActivity
    private var recordVideoDurationDialog: RecordVideoDurationDialog? = null
    private var listSelectionBottomSheet: ListSelectionBotDialog? = null
    private var showBottomSheet = false
    private var audioModel: AudioModel? = null
    private var recordSchedule: RecordSchedule? = null
    private var calendar = Calendar.getInstance()

    private val confirmCancelSchedule: ConfirmDialog by lazy {
        ConfirmDialog(object : ConfirmDialog.OnConfirmOke {
            override fun onConfirm() {
                viewModel.cancelSchedule()
            }
        }, object : OnDialogDismiss {
            override fun onDismiss() {

            }

        })
    }

    override val layoutId: Int = R.layout.layout_schedule_audio

    override fun initView() {
        mainActivity = activity as MainActivity

        binding!!.btnQuality.setOnClickListener(this)
        binding!!.btnMode.setOnClickListener(this)
        binding!!.btnDuration.setOnClickListener(this)
        binding!!.btnDate.setOnClickListener(this)
        binding!!.btnTime.setOnClickListener(this)
        binding!!.scheduleCard.cancelSchedule.setOnClickListener(this)
        binding!!.btnSetSchedule.setOnClickListener(this)

        binding!!.lifecycleOwner = this
        binding!!.viewModel = viewModel
        viewModel.getAudioScheduleConfig()
    }

    override fun initViewModel() {
        val factory = ScheduleAudioViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[ScheduleAudioViewModel::class.java]

        viewModel.recordAudioLiveData.observe(this) {
            audioModel = it
        }

        viewModel.recordScheduleLiveData.observe(this) {
            recordSchedule = it
        }

        viewModel.isRecordScheduleLiveData.observe(this) {
            if (it) {
                binding!!.scheduleCard.schedule = recordSchedule
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAudioConfig()
        viewModel.getSavedSchedule()
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding!!.btnQuality -> {
                if (!showBottomSheet) {
                    showBottomSheet = true
                    onQualityBottomSheet()
                }
            }
            binding!!.btnMode -> {
                if (!showBottomSheet) {
                    showBottomSheet = true
                    onModeBottomSheet()
                }
            }
            binding!!.btnDuration -> {
                if (!showBottomSheet) {
                    showBottomSheet = true
                    onDurationBottomSheet()
                }
            }
            binding!!.btnDate -> {
                onDatePicker()
            }
            binding!!.btnTime -> {
                onTimePicker()
            }
            binding!!.scheduleCard.cancelSchedule -> {
                val messages = resources.getString(R.string.video_record_schedule_cancel_message)
                confirmCancelSchedule.setMessages(messages)
                confirmCancelSchedule.show(requireActivity().supportFragmentManager, "Confirm")
            }
            binding!!.btnSetSchedule -> {
                setSchedule()
            }
        }
    }

    private fun setSchedule() {
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            Utils.showInvalidateTime(binding!!.root)
        } else {
            recordSchedule = RecordSchedule().apply {
                isVideo = false
                scheduleTime = calendar.timeInMillis
            }
            binding!!.scheduleCard.schedule = recordSchedule
            viewModel.setSchedule()
        }
    }

    private fun onQualityBottomSheet() {
        val listSelection = resources.getStringArray(R.array.record_quality).toMutableList()
        listSelectionBottomSheet = ListSelectionBotDialog(
            getString(R.string.record_quality),
            listSelection,
            object : ListSelectionAdapter.OnItemListSelection {
                override fun onSelection(position: Int) {
                    viewModel.updateQuality(AudioQuality.getByInt(position))
                    listSelectionBottomSheet!!.dialog!!.dismiss()
                }

            }, object : OnDialogDismiss {
                override fun onDismiss() {
                    showBottomSheet = false
                }

            })
        audioModel?.quality?.let { listSelectionBottomSheet!!.setSelectionPos(it.ordinal) }
        listSelectionBottomSheet!!.show(
            requireActivity().supportFragmentManager,
            "bottomSheetAudioRecordQuality"
        )
    }

    private fun onModeBottomSheet() {
        val listSelection = resources.getStringArray(R.array.record_mode).toMutableList()
        listSelectionBottomSheet = ListSelectionBotDialog(
            getString(R.string.record_mode),
            listSelection,
            object : ListSelectionAdapter.OnItemListSelection {
                override fun onSelection(position: Int) {
                    viewModel.updateMode(AudioMode.getByInt(position))
                    listSelectionBottomSheet!!.dialog!!.dismiss()
                }

            }, object : OnDialogDismiss {
                override fun onDismiss() {
                    showBottomSheet = false
                }

            })
        audioModel?.mode?.let { listSelectionBottomSheet!!.setSelectionPos(it.ordinal) }
        listSelectionBottomSheet!!.show(
            requireActivity().supportFragmentManager,
            "bottomSheetAudioRecordMode"
        )
    }

    private fun onDurationBottomSheet() {
        recordVideoDurationDialog =
            RecordVideoDurationDialog(object : RecordVideoDurationDialog.OnSelectDuration {
                override fun onSelectDuration(duration: Long) {
                    viewModel.updateDuration(duration)
                }
            },
                object : OnDialogDismiss {
                    override fun onDismiss() {
                        showBottomSheet = false
                    }
                })
        recordVideoDurationDialog!!.setupTotalTime(audioModel!!.duration)
        recordVideoDurationDialog!!.show(
            requireActivity().supportFragmentManager,
            "VideoDuration"
        )
    }

    private fun onDatePicker() {
        val curDay = calendar.get(Calendar.DAY_OF_MONTH)
        val curMonth = calendar.get(Calendar.MONTH)
        val curYear = calendar.get(Calendar.YEAR)
        val datePicker = DatePickerDialog(
            requireContext(), R.style.TimeAndDatePickerStyle,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                if (calendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                    Utils.showInvalidateTime(binding!!.root)
                } else {
                    viewModel.updateDate(calendar.timeInMillis)
                }
            }, curYear, curMonth, curDay
        )
        datePicker.datePicker.minDate = Calendar.getInstance().timeInMillis
        datePicker.show()
    }

    private fun onTimePicker() {
        val curHour = calendar.get(Calendar.HOUR_OF_DAY)
        val curMinute = calendar.get(Calendar.MINUTE)
        val timePicker = TimePickerDialog(
            requireContext(), R.style.TimeAndDatePickerStyle,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                if (calendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                    Utils.showInvalidateTime(binding!!.root)
                } else {
                    viewModel.updateTime(calendar.timeInMillis)
                }
            }, curHour, curMinute, true
        )
        timePicker.show()
    }
}