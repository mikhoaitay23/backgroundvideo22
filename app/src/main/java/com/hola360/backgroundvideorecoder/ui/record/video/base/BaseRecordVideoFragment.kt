package com.hola360.backgroundvideorecoder.ui.record.video.base

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.dialog.*
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.ui.record.RecordSchedule
import com.hola360.backgroundvideorecoder.ui.record.video.RecordVideo
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.PathUtils
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
    private var showBatteryAlertDialog=false
    private var showStorageAlertDialog=false
    private val alertDialog: RecordAlertDialog by lazy {
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
        (requireActivity() as MainActivity).recordService!!.cancelAlarmSchedule(requireContext(), true)
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
        if (SystemUtils.isAndroidM() && videoConfiguration!!.previewMode) {
            requestOverlayPermission()
        }
    }

    protected fun checkPreviewMode(){
        if (videoConfiguration!!.previewMode) {
            if (SystemUtils.isAndroidM() && !Settings.canDrawOverlays(requireContext())) {
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

    protected fun startRecordOrSetSchedule() {
        if (SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)) {
            setupWhenCameraPermissionGranted()
        } else {
            requestCameraPermission.launch(Constants.CAMERA_RECORD_PERMISSION)
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String?, Boolean?>? ->
        if (SystemUtils.hasPermissions(requireContext(), *Constants.CAMERA_RECORD_PERMISSION)) {
            setupWhenCameraPermissionGranted()
        } else {
            SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
        }
    }

    private fun setupWhenCameraPermissionGranted(){
        if (FilePickerUtils.storagePermissionGrant(requireContext())) {
            setupWhenStoragePermissionGranted()
        } else {
            requestPermission()
        }
    }

    private fun requestOverlayPermission() {
        if (SystemUtils.isAndroidM() && !Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + requireContext().packageName)
            )
            startActivityForResult(intent, 0)
        }
    }

    abstract fun startAction()

    protected fun onLowBatteryAction(){
        if((requireActivity() as MainActivity).supportFragmentManager.findFragmentByTag(RecordVideo.ALERT_TAG) == null){
            if(!showBatteryAlertDialog){
                showBatteryAlertDialog=true
                alertDialog.isBattery= true
                alertDialog.show((requireActivity() as MainActivity).supportFragmentManager,
                    RecordVideo.ALERT_TAG
                )
            }
        }
    }

    protected fun onLowStorageAction(){
        if((requireActivity() as MainActivity).supportFragmentManager.findFragmentByTag(RecordVideo.ALERT_TAG) == null){
            if(!showStorageAlertDialog){
                showStorageAlertDialog=true
                alertDialog.isBattery= false
                alertDialog.show((requireActivity() as MainActivity).supportFragmentManager,
                    RecordVideo.ALERT_TAG
                )
            }
        }
    }

    protected fun onStopRecord(){
        showBatteryAlertDialog=false
        showStorageAlertDialog=false
    }

    private fun requestPermission() {
        if (SystemUtils.isAndroidR()) {
            activityResultLauncher.launch(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        } else resultLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun setupWhenStoragePermissionGranted(){
        PathUtils.setParentPath(requireContext())
        if (SystemUtils.isAndroidM() &&!Settings.canDrawOverlays(requireContext())) {
            requestOverlayPermission()
        } else {
            startAction()
        }
    }

    private val customContract = object : ActivityResultContract<String, Boolean>() {
        override fun createIntent(context: Context, input: String): Intent {
            val intent = Intent(input)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse(
                String.format(
                    "package:%s",
                    requireActivity().applicationContext.packageName
                )
            )
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            if (SystemUtils.isAndroidR()) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

    }

    private val activityResultLauncher = registerForActivityResult(customContract) {
        if (it) {
            setupWhenStoragePermissionGranted()
        } else {
            SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (FilePickerUtils.storagePermissionGrant(requireContext())
            ) {
                setupWhenStoragePermissionGranted()
            } else {
                SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
            }
        }

    companion object {
        const val CAMERA_FACING_FRONT = 0
        const val CAMERA_FACING_BACK = 1
    }

}