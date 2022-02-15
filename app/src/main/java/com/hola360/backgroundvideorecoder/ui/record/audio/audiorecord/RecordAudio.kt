package com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord

import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordAudioBinding
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet.AudioRecordBottomSheetFragment
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils

class RecordAudio : BaseRecordPageFragment<LayoutRecordAudioBinding>(), View.OnClickListener,
    RecordService.Listener {

    private var audioModel: AudioModel? = null
    private var recordVideoDurationDialog: RecordVideoDurationDialog? = null
    private lateinit var mainActivity: MainActivity
    private var listSelectionBottomSheet: ListSelectionBotDialog? = null
    private lateinit var viewModel: RecordAudioViewModel
    private var showBottomSheet = false
    private var audioRecordBottomSheetFragment: AudioRecordBottomSheetFragment? = null
    private var isShow = false

    override val layoutId: Int = R.layout.layout_record_audio

    override fun initViewModel() {
        val factory = RecordAudioViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[RecordAudioViewModel::class.java]

        viewModel.recordAudioLiveData.observe(this) {
            audioModel = it
        }

    }

    override fun initView() {
        mainActivity = activity as MainActivity

        binding!!.btnQuality.setOnClickListener(this)
        binding!!.btnMode.setOnClickListener(this)
        binding!!.btnDuration.setOnClickListener(this)
        binding!!.imgRecord.setOnClickListener(this)

        binding!!.lifecycleOwner = this
        binding!!.viewModel = viewModel

        if (mainActivity.recordService!!.isRecording()) {
            onAudioRecordBottomSheet()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAudioConfig()
        if (mainActivity.recordService == null) {
            mainActivity.bindService()
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                onServiceStart()
            } else {
                SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
            }
        }

    override fun onClick(p0: View?) {
        when (p0) {
            binding!!.imgRecord -> {
                if (SystemUtils.hasPermissions(
                        requireContext(),
                        Constants.RECORD_AUDIO_PERMISSION
                    )
                ) {
                    onServiceStart()
                } else {
                    resultLauncher.launch(Constants.RECORD_AUDIO_PERMISSION)
                }
            }
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

    private fun onServiceStart() {
        mainActivity.handleRecordStatus(MainActivity.AUDIO_RECORD)
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

    private fun onAudioRecordBottomSheet() {
        if (!isShow) {
            audioRecordBottomSheetFragment =
                AudioRecordBottomSheetFragment(object : OnDialogDismiss {
                    override fun onDismiss() {
                        isShow = false
                    }

                })
            audioRecordBottomSheetFragment!!.show(
                requireActivity().supportFragmentManager,
                "VideoDuration"
            )
            isShow = true
        }
    }

    override fun onRecordStarted(status: Int) {

    }

    override fun updateRecordTime(time: Long, status: Int) {

    }

    override fun onRecordCompleted() {

    }

    override fun onUpdateTime(fileName: String, duration: Long, curTime: Long) {

    }

    override fun onStopped() {

    }

    override fun onByteBuffer(buf: ShortArray?, minBufferSize: Int) {

    }


}