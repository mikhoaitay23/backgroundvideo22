package com.hola360.backgroundvideorecoder.ui.record.video.base

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.ViewDataBinding
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog
import com.hola360.backgroundvideorecoder.ui.dialog.ConfirmDialog
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog
import com.hola360.backgroundvideorecoder.ui.dialog.VideoIntervalDurationDialog
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordSchedule
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils


abstract class BaseRecordVideoFragment<V : ViewDataBinding?> : BaseRecordPageFragment<V>() {

    protected var videoConfiguration: VideoRecordConfiguration? = null
    protected var recordSchedule: RecordSchedule? = null
    protected val cameraSelectionDialog: ListSelectionBotDialog by lazy {
        val title = resources.getString(R.string.video_record_configuration_camera)
        val itemList = resources.getStringArray(R.array.camera_facing).toMutableList()
        ListSelectionBotDialog(title, itemList, object : ListSelectionAdapter.OnItemListSelection {
            override fun onSelection(position: Int) {
                videoConfiguration!!.isBack = position == CAMERA_FACING_BACK
                saveNewVideoConfiguration()
                applyNewVideoConfiguration()
                cameraSelectionDialog.dialog?.dismiss()
            }
        }, dismissCallback)
    }
    private val recordVideoDurationDialog: RecordVideoDurationDialog by lazy {
        RecordVideoDurationDialog(object : RecordVideoDurationDialog.OnSelectDuration {
            override fun onSelectDuration(duration: Long) {
                videoConfiguration!!.totalTime = duration
                saveNewVideoConfiguration()
                applyNewVideoConfiguration()
            }
        }, dismissCallback)
    }
    private val videoIntervalDurationDialog: VideoIntervalDurationDialog by lazy {
        VideoIntervalDurationDialog(object : RecordVideoDurationDialog.OnSelectDuration {
            override fun onSelectDuration(duration: Long) {
                videoConfiguration!!.timePerVideo = duration
                saveNewVideoConfiguration()
                applyNewVideoConfiguration()
            }
        }, dismissCallback)
    }
    private val confirmCancelSchedule: ConfirmDialog by lazy {
        ConfirmDialog(object : ConfirmDialog.OnConfirmOke {
            override fun onConfirm() {
                onCancelSchedule()
            }
        }, dismissCallback)
    }
    protected val dismissCallback = object : OnDialogDismiss {
        override fun onDismiss() {
            showDialog = false
        }
    }
    protected var switchThumb: Int = 0
    protected var showDialog = false
    protected var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoConfiguration = VideoRecordUtils.getVideoConfiguration(requireContext())
        recordSchedule = VideoRecordUtils.getVideoSchedule(requireContext())
    }

    abstract fun updateSwitchThumb()

    abstract fun generateCancelDialogMessages(): String

    abstract fun onCancelSchedule()

    protected fun cancelSchedule() {
        recordSchedule = RecordSchedule()
        dataPref!!.putSchedule("")
        (requireActivity() as MainActivity).handleRecordStatus(MainActivity.CANCEL_SCHEDULE_RECORD_VIDEO)
        VideoRecordUtils.cancelAlarmSchedule(requireContext())
    }

    override fun onResume() {
        super.onResume()
        videoConfiguration = VideoRecordUtils.getVideoConfiguration(requireContext())
        recordSchedule = VideoRecordUtils.getVideoSchedule(requireContext())
        applyNewVideoConfiguration()
    }

    protected fun saveNewVideoConfiguration() {
        val configurationString = Gson().toJson(videoConfiguration!!)
        configurationString?.let {
            dataPref!!.putVideoConfiguration(it)
        }
    }

    abstract fun applyNewVideoConfiguration()

    protected fun onCameraFacingSelect() {
        if (!showDialog) {
            showDialog = true
            val position = if (videoConfiguration!!.isBack) {
                CAMERA_FACING_BACK
            } else {
                CAMERA_FACING_FRONT
            }
            cameraSelectionDialog.setSelectionPos(position)
            cameraSelectionDialog.show(requireActivity().supportFragmentManager, "Camera")
        }
    }

    protected fun onVideoRecordDurationSelect() {
        if (!showDialog) {
            showDialog = true
            recordVideoDurationDialog.setupTotalTime(videoConfiguration!!.totalTime)
            recordVideoDurationDialog.show(
                requireActivity().supportFragmentManager,
                "VideoDuration"
            )
        }
    }

    protected fun onVideoIntervalSelect() {
        if (!showDialog) {
            showDialog = true
            videoIntervalDurationDialog.setupIntervalTime(videoConfiguration!!.timePerVideo)
            videoIntervalDurationDialog.show(
                requireActivity().supportFragmentManager,
                "IntervalTime"
            )
        }
    }

    protected fun onPreviewModeChange() {
        videoConfiguration!!.previewMode = !videoConfiguration!!.previewMode
        applyNewVideoConfiguration()
        saveNewVideoConfiguration()
        if (SystemUtils.isAndroidO() && videoConfiguration!!.previewMode) {
            requestOverlayPermission()
        }
    }

    protected fun checkPreviewMode(){
        if (videoConfiguration!!.previewMode) {
            if (SystemUtils.isAndroidO() && !Settings.canDrawOverlays(requireContext())) {
                videoConfiguration!!.previewMode = false
                applyNewVideoConfiguration()
                saveNewVideoConfiguration()
            }
        }
    }

    protected fun onFlashModeChange() {
        videoConfiguration!!.flash = !videoConfiguration!!.flash
        applyNewVideoConfiguration()
        saveNewVideoConfiguration()
    }

    protected fun onSoundModeChange() {
        videoConfiguration!!.sound = !videoConfiguration!!.sound
        applyNewVideoConfiguration()
        saveNewVideoConfiguration()
    }

    protected fun showCancelDialog() {
        if (!showDialog) {
            showDialog = true
            confirmCancelSchedule.setMessages(generateCancelDialogMessages())
            confirmCancelSchedule.show(requireActivity().supportFragmentManager, "Confirm")
        }
    }

    protected val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String?, Boolean?>? ->
        if (SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)) {
            requestOverlayPermission()
        } else {
            SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
        }
    }

    protected fun requestOverlayPermission() {
        if (SystemUtils.isAndroidO() && !Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + requireContext().packageName)
            )
            startActivityForResult(intent, 0)
        }
    }

    protected fun startRecordOrSetSchedule() {
        if (SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)) {
            if (SystemUtils.isAndroidO() &&!Settings.canDrawOverlays(requireContext())) {
                requestOverlayPermission()
            } else {
                startAction()
            }
        } else {
            requestCameraPermission.launch(Constants.CAMERA_RECORD_PERMISSION)
        }
    }

    abstract fun startAction()

    companion object {
        const val CAMERA_FACING_FRONT = 0
        const val CAMERA_FACING_BACK = 1
    }

}