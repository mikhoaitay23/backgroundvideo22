package com.hola360.backgroundvideorecoder.ui.dialog

import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutVideoIntervalDurationBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog

class VideoZoomDialog(
    private val callback: OnSelectZoomScale,
    private val dismissCallback:OnDialogDismiss): BaseBottomSheetDialog<LayoutVideoIntervalDurationBinding>() {

    private var progress:Int=0

    override fun initView() {
        binding!!.isZoom=true
        binding!!.seekbar.max=10
        setupProgress()
        binding!!.seekbar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                this@VideoZoomDialog.progress= progress
                setupToolTipText()
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
            callback.onSelectZoomScale(progress.toFloat()/10)
            dismiss()
        }
    }

    fun setupZoomValue(zoomScale: Float){
        this.progress= (zoomScale*10).toInt()
    }

    private fun setupProgress(){
        binding!!.seekbar.progress= progress
        setupToolTipText()
        Handler(Looper.getMainLooper()).postDelayed({
            setupToolTipText()
        }, 100)

    }

    private fun setupToolTipText(){
        binding!!.txtProgress.text= (progress.toFloat()/10).toString()
        val position= (((binding!!.seekbar.right - binding!!.seekbar.left- 2*resources.getDimensionPixelSize(R.dimen.home_v_margin)).toFloat()/ binding!!.seekbar.max) * progress ).toInt() - binding!!.txtProgress.width/2*(progress/binding!!.seekbar.max.toFloat())
        binding!!.txtProgress.x= position
    }

    override fun getLayout(): Int {
        return R.layout.layout_video_interval_duration
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissCallback.onDismiss()
    }

    interface OnSelectZoomScale{
        fun onSelectZoomScale(scale:Float)
    }

}