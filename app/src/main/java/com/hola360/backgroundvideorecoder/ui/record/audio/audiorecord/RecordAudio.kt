package com.hola360.backgroundvideorecoder.ui.record.audio.audiorecord

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.anggrayudi.storage.file.getAbsolutePath
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordAudioBinding
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BasePermissionRequestFragment
import com.hola360.backgroundvideorecoder.ui.dialog.ConfirmDialog
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog
import com.hola360.backgroundvideorecoder.ui.dialog.WarningDialog
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.StorageUtils
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet.AudioRecordBottomSheetFragment
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import com.hola360.backgroundvideorecoder.utils.Utils

class RecordAudio : BasePermissionRequestFragment<LayoutRecordAudioBinding>(), View.OnClickListener,
    RecordService.Listener {

    private var audioModel: AudioModel? = null
    private var mRecordVideoDurationDialog: RecordVideoDurationDialog? = null
    private var mListSelectionBottomSheet: ListSelectionBotDialog? = null
    private var mAudioRecordBottomSheetFragment: AudioRecordBottomSheetFragment? = null
    private lateinit var viewModel: RecordAudioViewModel
    private var showBottomSheet = false
    private var isShow = false

    override val layoutId: Int = R.layout.layout_record_audio
    override val showToolbar: Boolean = true
    override val toolbarTitle: String = "Audio Record"
    override val menuCode: Int = 0

    override fun initViewModel() {
        val factory = RecordAudioViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[RecordAudioViewModel::class.java]

        viewModel.recordAudioLiveData.observe(this) {
            audioModel = it
        }

    }

    override fun initView() {
        binding!!.btnQuality.setOnClickListener(this)
        binding!!.btnMode.setOnClickListener(this)
        binding!!.btnDuration.setOnClickListener(this)
        binding!!.imgRecord.setOnClickListener(this)

        binding!!.lifecycleOwner = this
        binding!!.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAudioConfig()
        mainActivity.recordService!!.registerListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        isShow = false
    }

    private val resultRecordPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (Utils.recordPermissionGrant(requireContext())
            ) {
                startRecord()
            } else {
                showAlertPermissionNotGrant()
            }
        }

    override fun onClick(p0: View?) {
        when (p0) {
            binding!!.imgRecord -> {
                if (mainActivity.recordService!!.isRecording()) {
                    mainActivity.showToast(getString(R.string.recording_alert))
                } else {
                    if (Utils.storagePermissionGrant(requireContext())) {
//                        if (!SharedPreferenceUtils.getInstance(requireContext())?.getSchedule()
//                                .isNullOrEmpty()
//                        ) {
//                            val mRecordSchedule = Gson().fromJson(
//                                SharedPreferenceUtils.getInstance(requireContext())?.getSchedule(),
//                                RecordSchedule::class.java
//                            )
//                            if (mRecordSchedule.scheduleTime > 0 && System.currentTimeMillis()
//                                    .plus(audioModel!!.duration) > mRecordSchedule.scheduleTime
//                            ) {
//
//                            } else {
//                                record()
//                            }
//                        } else {
//                            record()
//                        }
                        record()
                    } else {
                        requestPermission()
                    }
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

    private fun record() {
        if (Utils.recordPermissionGrant(requireContext())) {
            startRecord()
        } else {
            resultRecordPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    private fun startRecord() {
        val parentPath = SharedPreferenceUtils.getInstance(mainActivity)!!.getParentPath()
        if (parentPath.isNullOrEmpty()) {
            val storageList = StorageUtils.getAllStorages(mainActivity)
            val defaultPath =
                storageList[0].getRootDocumentationFile(mainActivity).getAbsolutePath(mainActivity)
            SharedPreferenceUtils.getInstance(mainActivity)!!.setParentPath(defaultPath)
        }
        mainActivity.recordService!!.startRecordAudio()
        mainActivity.recordService!!.registerListener(this)
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

    private fun onAudioRecordBottomSheet() {
        if (!isShow) {
            mAudioRecordBottomSheetFragment =
                AudioRecordBottomSheetFragment(object : OnDialogDismiss {
                    override fun onDismiss() {
                        isShow = false
                    }

                })
            mAudioRecordBottomSheetFragment!!.show(
                requireActivity().supportFragmentManager,
                "VideoDuration"
            )
            mAudioRecordBottomSheetFragment!!.isCancelable = false
            isShow = true
        }
    }

    override fun onUpdateTime(fileName: String, duration: Long, curTime: Long) {
        if (mainActivity.isBound && mainActivity.recordService!!.isRecording()
            && mainActivity.window.decorView.isShown && isVisible && isAdded
        ) {
            onAudioRecordBottomSheet()
        }
    }

    override fun onStopped() {

    }

    override fun onByteBuffer(buf: ShortArray?, minBufferSize: Int) {

    }

    override fun onBatteryLow(batteryPer: Float) {

    }

    override fun setupWhenPermissionGranted() {
        record()
    }


}