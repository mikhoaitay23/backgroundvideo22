package com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentBottomSheetRecordAudioBinding
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog
import com.hola360.backgroundvideorecoder.ui.dialog.ConfirmDialog
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.WarningDialog
import com.hola360.backgroundvideorecoder.utils.Utils
import com.hola360.backgroundvideorecoder.widget.AudioView
import com.hola360.backgroundvideorecoder.widget.bottomsheet.confirm.ConfirmBottomSheetFragment
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

class AudioRecordBottomSheetFragment(val dismissCallback: OnDialogDismiss) :
    BaseBottomSheetDialog<FragmentBottomSheetRecordAudioBinding>(), View.OnClickListener,
    ConfirmBottomSheetFragment.OnConfirmButtonClickListener, RecordService.Listener {

    private lateinit var viewModel: AudioRecordBottomSheetViewModel
    private lateinit var mainActivity: MainActivity
    private var confirmBottomSheetFragment: ConfirmBottomSheetFragment? = null
    private var warningDialog: WarningDialog? = null

    override fun getLayout() = R.layout.fragment_bottom_sheet_record_audio

    override fun initView() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        val factory = AudioRecordBottomSheetViewModel.Factory(requireActivity().application)
        viewModel =
            ViewModelProvider(this, factory)[AudioRecordBottomSheetViewModel::class.java]

        mainActivity.recordService?.registerListener(this)
        initClick()

    }

    override fun onDestroy() {
        super.onDestroy()
        dismissCallback.onDismiss()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding!!.btnPause -> {

            }
            binding!!.btnAbort -> {
                confirmBottomSheetFragment = ConfirmBottomSheetFragment.create(
                    ConfirmBottomSheetFragment.DataBuilder().addTitle(getString(R.string.confirm))
                        .addMsg(getString(R.string.abort_msg)).addPositive(getString(R.string.ok))
                        .addNegative(getString(R.string.cancel))
                )
                confirmBottomSheetFragment!!.show(
                    requireActivity().supportFragmentManager,
                    "bottomSheetAbortAudioRecord"
                )
            }
            binding!!.btnSave -> {
                mainActivity.recordService?.stopRecording()
            }
        }
    }

    private fun initClick() {
        binding!!.btnPause.setOnClickListener(this)
        binding!!.btnAbort.setOnClickListener(this)
        binding!!.btnSave.setOnClickListener(this)
    }

    override fun onUpdateTime(fileName: String, duration: Long, curTime: Long) {
        if (mainActivity.isBound && mainActivity.recordService!!.isRecording()) {
            binding!!.tvTime.text = Utils.convertTime(curTime / 1000)
            if (fileName.isNotEmpty() && fileName != "") {
                binding!!.tvTitle.text = fileName
            }
        }
    }

    override fun onStopped() {
        dismissAllowingStateLoss()
    }

    override fun onByteBuffer(buf: ShortArray?, minBufferSize: Int) {
        binding!!.audioVisualizer.setStyle(
            AudioView.ShowStyle.STYLE_HOLLOW_LUMP,
            AudioView.ShowStyle.STYLE_HOLLOW_LUMP
        )
        binding!!.audioVisualizer.setWaveData(buf)
    }

    override fun onBatteryLow(batteryPer: Float) {
        warningDialog = WarningDialog(object : ConfirmDialog.OnConfirmOke {
            override fun onConfirm() {
                mainActivity.recordService!!.stopRecording()
            }

        }, object : OnDialogDismiss {
            override fun onDismiss() {
                warningDialog!!.dismiss()
            }

        })
        warningDialog!!.setBatteryOrStorageType(true)
        warningDialog!!.show(requireActivity().supportFragmentManager, "DialogBatteryLow")
    }

    override fun onPositiveClick() {

    }

    override fun onNegativeClick() {

    }
}