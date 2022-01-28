package com.hola360.backgroundvideorecoder.ui.record.audio.bottomsheet

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.databinding.FragmentBottomSheetRecordAudioBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils
import com.hola360.backgroundvideorecoder.utils.Constants
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import com.zlw.main.recorderlib.BuildConfig
import com.zlw.main.recorderlib.RecordManager
import com.zlw.main.recorderlib.recorder.RecordHelper.RecordState
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener
import java.util.*

class AudioRecordBottomSheetFragment :
    BaseBottomSheetDialog<FragmentBottomSheetRecordAudioBinding>(), View.OnClickListener, AudioRecordUtils.Listener {

    private lateinit var viewModel: AudioRecordBottomSheetViewModel
    private var audioRecordUtils: AudioRecordUtils? = null
    private var audioModel: AudioModel? = null
    private var recordManager = RecordManager.getInstance()
    private lateinit var mainActivity: MainActivity

    override fun getLayout() = R.layout.fragment_bottom_sheet_record_audio

    override fun initView() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        val factory = AudioRecordBottomSheetViewModel.Factory(requireActivity().application)
        viewModel =
            ViewModelProvider(this, factory)[AudioRecordBottomSheetViewModel::class.java]
        audioRecordUtils = AudioRecordUtils()
        audioRecordUtils?.registerListener(this)
        audioModel = AudioModel()

        initClick()
        initRecord()

        if (SystemUtils.hasPermissions(
                requireContext(),
                *Constants.STORAGE_PERMISSION_UNDER_STORAGE_SCOPE
            )
        ) {
            audioRecordUtils?.onStartRecording(audioModel!!)
        } else {
            resultLauncher.launch(Constants.STORAGE_PERMISSION_UNDER_STORAGE_SCOPE)
        }

    }

    override fun onResume() {
        super.onResume()
        initRecordEvent()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding!!.btnPause -> {
                audioRecordUtils?.onStartRecording(audioModel!!)
            }
            binding!!.btnAbort -> {

            }
            binding!!.btnSave -> {
                audioRecordUtils?.onStopRecording()
                dismissAllowingStateLoss()
            }
        }
    }

    private fun initClick() {
        binding!!.btnPause.setOnClickListener(this)
        binding!!.btnAbort.setOnClickListener(this)
        binding!!.btnSave.setOnClickListener(this)
    }

    private fun initRecord() {
        recordManager!!.init(mainActivity.application, BuildConfig.DEBUG)
        val recordDir = String.format(
            Locale.getDefault(), "%s/Record/backgroundrecord/",
            Environment.getExternalStorageDirectory().absolutePath
        )
        recordManager.changeRecordDir(recordDir)
        initRecordEvent()
    }

    private fun initRecordEvent() {
        recordManager.setRecordStateListener(object : RecordStateListener {
            override fun onStateChange(state: RecordState) {
                when (state) {
                    RecordState.PAUSE -> {
                        binding!!.btnRecord.setImageResource(R.drawable.ic_record_normal)
                        binding!!.tvRecord.text = getString(R.string.resume)
                    }
                    RecordState.RECORDING -> {
                        binding!!.btnRecord.setImageResource(R.drawable.ic_record_pause)
                        binding!!.tvRecord.text = getString(R.string.pause)
                    }
                }
            }

            override fun onError(error: String) {
                Log.d("TAG", "onError: $error")
            }
        })
        recordManager.setRecordSoundSizeListener {

        }

        recordManager.setRecordResultListener {
            Toast.makeText(
                requireContext(),
                "Save Successful： " + it.absolutePath,
                Toast.LENGTH_SHORT
            ).show()
        }
        recordManager.setRecordFftDataListener { data -> binding!!.audioVisualizer.setWaveData(data) }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String?, Boolean?>? ->
            if (SystemUtils.hasPermissions(
                    requireContext(),
                    *Constants.STORAGE_PERMISSION_UNDER_STORAGE_SCOPE
                )
            ) {
                audioRecordUtils?.onStartRecording(audioModel!!)
            } else {
                SystemUtils.showAlertPermissionNotGrant(binding!!, requireActivity())
            }
        }

    override fun updateTimer(time: Long) {
        binding!!.tvTime.text = Utils.formatTimeIntervalHourMinSec(time)
    }


}