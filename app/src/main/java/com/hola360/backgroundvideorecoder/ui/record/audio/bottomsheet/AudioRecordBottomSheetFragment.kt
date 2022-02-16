package com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.databinding.FragmentBottomSheetRecordAudioBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.widget.bottomsheet.confirm.ConfirmBottomSheetFragment
import java.util.*

class AudioRecordBottomSheetFragment(val dismissCallback: OnDialogDismiss) :
    BaseBottomSheetDialog<FragmentBottomSheetRecordAudioBinding>(), View.OnClickListener,
    ConfirmBottomSheetFragment.OnConfirmButtonClickListener {

    private lateinit var viewModel: AudioRecordBottomSheetViewModel
    private var audioModel: AudioModel? = null
    private lateinit var mainActivity: MainActivity
    private var confirmBottomSheetFragment: ConfirmBottomSheetFragment? = null

    override fun getLayout() = R.layout.fragment_bottom_sheet_record_audio

    override fun initView() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        val factory = AudioRecordBottomSheetViewModel.Factory(requireActivity().application)
        viewModel =
            ViewModelProvider(this, factory)[AudioRecordBottomSheetViewModel::class.java]
        audioModel = AudioModel()

        initClick()

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissCallback.onDismiss()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding!!.btnPause -> {
//                if (recordManager.getState() == RecordHelper.RecordState.RECORDING) {
//                    (requireActivity() as MainActivity).handleRecordStatus(MainActivity.AUDIO_PAUSE)
//                } else {
//                    (requireActivity() as MainActivity).handleRecordStatus(MainActivity.AUDIO_RESUME)
//                }
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
//                if (recordManager.getState() != RecordHelper.RecordState.IDLE) {
//                    (requireActivity() as MainActivity).handleRecordStatus(MainActivity.AUDIO_STOP)
//                }
//                dismissAllowingStateLoss()
            }
        }
    }

    private fun initClick() {
        binding!!.btnPause.setOnClickListener(this)
        binding!!.btnAbort.setOnClickListener(this)
        binding!!.btnSave.setOnClickListener(this)
    }

//    override fun updateTimer(time: Long) {
//        binding!!.tvTime.text = Utils.formatTimeIntervalHourMinSec(time)
//    }

    override fun onPositiveClick() {

    }

    override fun onNegativeClick() {

    }


}