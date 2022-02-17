package com.hola360.backgroundvideorecoder.ui.setting.audiosetting

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.databinding.LayoutSettingAudioBinding
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord.RecordAudioViewModel
import com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet.AudioRecordBottomSheetFragment

class AudioSetting : BaseRecordPageFragment<LayoutSettingAudioBinding>(), View.OnClickListener {

    private lateinit var viewModel: AudioSettingViewModel
    private var audioModel: AudioModel? = null
    private var showBottomSheet = false
    private var mRecordVideoDurationDialog: RecordVideoDurationDialog? = null
    private var mListSelectionBottomSheet: ListSelectionBotDialog? = null

    override val layoutId: Int
        get() = R.layout.layout_setting_audio

    override fun initView() {
        binding!!.btnQuality.setOnClickListener(this)
        binding!!.btnMode.setOnClickListener(this)
        binding!!.btnDuration.setOnClickListener(this)

        binding!!.lifecycleOwner = this
        binding!!.viewModel = viewModel
    }

    override fun initViewModel() {
        val factory = AudioSettingViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[AudioSettingViewModel::class.java]

        viewModel.recordAudioLiveData.observe(this) {
            audioModel = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getAudioConfig()
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
        mListSelectionBottomSheet = ListSelectionBotDialog(
            getString(R.string.record_quality),
            listSelection,
            object : ListSelectionAdapter.OnItemListSelection {
                override fun onSelection(position: Int) {
                    viewModel.updateQuality(AudioQuality.getByInt(position))
                    mListSelectionBottomSheet!!.dialog!!.dismiss()
                }

            }, object : OnDialogDismiss {
                override fun onDismiss() {
                    showBottomSheet = false
                }

            })
        audioModel?.quality?.let { mListSelectionBottomSheet!!.setSelectionPos(it.ordinal) }
        mListSelectionBottomSheet!!.show(
            requireActivity().supportFragmentManager,
            "bottomSheetAudioRecordQuality"
        )
    }

    private fun onModeBottomSheet() {
        val listSelection = resources.getStringArray(R.array.record_mode).toMutableList()
        mListSelectionBottomSheet = ListSelectionBotDialog(
            getString(R.string.record_mode),
            listSelection,
            object : ListSelectionAdapter.OnItemListSelection {
                override fun onSelection(position: Int) {
                    viewModel.updateMode(AudioMode.getByInt(position))
                    mListSelectionBottomSheet!!.dialog!!.dismiss()
                }

            }, object : OnDialogDismiss {
                override fun onDismiss() {
                    showBottomSheet = false
                }

            })
        audioModel?.mode?.let { mListSelectionBottomSheet!!.setSelectionPos(it.ordinal) }
        mListSelectionBottomSheet!!.show(
            requireActivity().supportFragmentManager,
            "bottomSheetAudioRecordMode"
        )
    }

    private fun onDurationBottomSheet() {
        mRecordVideoDurationDialog =
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
        mRecordVideoDurationDialog!!.setupTotalTime(audioModel!!.duration)
        mRecordVideoDurationDialog!!.show(
            requireActivity().supportFragmentManager,
            "VideoDuration"
        )
    }

}