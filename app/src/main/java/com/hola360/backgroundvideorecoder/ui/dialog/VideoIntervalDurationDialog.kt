package com.hola360.backgroundvideorecoder.ui.dialog

import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutVideoIntervalDurationBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog

class VideoIntervalDurationDialog(val callback: RecordVideoDurationDialog.OnSelectDuration,
                                  private val dismissCallback:OnDialogDismiss): BaseBottomSheetDialog<LayoutVideoIntervalDurationBinding>() {

    private var intervalTime:Long=0

    override fun initView() {
        binding!!.seekbar.max=9
        setupProgress(intervalTime)
        binding!!.seekbar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setupToolTipText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding!!.cancel.setOnClickListener {
            dismiss()
        }
        binding!!.oke.setOnClickListener {
            callback.onSelectDuration((binding!!.seekbar.progress+1)*60000L)
            dismiss()
        }
    }

    fun setupIntervalTime(intervalTime: Long){
        this.intervalTime= intervalTime
    }

    private fun setupProgress(intervalTime: Long){
        val progress= intervalTime.toInt()/60000-1
        binding!!.seekbar.progress= progress
        Handler(Looper.getMainLooper()).postDelayed({
            setupToolTipText(progress)
        }, 100)

    }

    private fun setupToolTipText(progress:Int){
        binding!!.txtProgress.text= (progress+1).toString()
        val position= (((binding!!.seekbar.right - binding!!.seekbar.left- 2*resources.getDimensionPixelSize(R.dimen.home_v_margin)).toFloat()/ binding!!.seekbar.max) * progress ).toInt()
        binding!!.txtProgress.x= position.toFloat()
    }

    override fun getLayout(): Int {
        return R.layout.layout_video_interval_duration
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissCallback.onDismiss()
    }

}