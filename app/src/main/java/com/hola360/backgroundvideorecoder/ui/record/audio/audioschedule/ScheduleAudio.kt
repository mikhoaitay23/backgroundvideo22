package com.hola360.backgroundvideorecoder.ui.record.audio.audioschedule

import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.databinding.LayoutScheduleAudioBinding
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment

class ScheduleAudio : BaseRecordPageFragment<LayoutScheduleAudioBinding>(), View.OnClickListener {

    private lateinit var viewModel: ScheduleAudioViewModel
    private lateinit var mainActivity: MainActivity
    private var recordVideoDurationDialog: RecordVideoDurationDialog? = null
    private var listSelectionBottomSheet: ListSelectionBotDialog? = null
    private var showBottomSheet = false
    private var audioModel: AudioModel? = null

    override val layoutId: Int = R.layout.layout_schedule_audio

    override fun initView() {
        mainActivity = activity as MainActivity

        binding!!.btnQuality.setOnClickListener(this)
        binding!!.btnMode.setOnClickListener(this)
        binding!!.btnDuration.setOnClickListener(this)

        binding!!.lifecycleOwner = this
        binding!!.viewModel = viewModel
        viewModel.getAudioConfig()
    }

    override fun initViewModel() {
        val factory = ScheduleAudioViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[ScheduleAudioViewModel::class.java]

        viewModel.recordAudioLiveData.observe(this, {
            audioModel = it
        })
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
}