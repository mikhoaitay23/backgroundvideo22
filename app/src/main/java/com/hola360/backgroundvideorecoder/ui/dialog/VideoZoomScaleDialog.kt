package com.hola360.backgroundvideorecoder.ui.dialog

import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoDurationBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog

class VideoZoomScaleDialog(val callback: OnSelectZoomScale,
                           private val dismissCallback: OnDialogDismiss
):BaseBottomSheetDialog<LayoutRecordVideoDurationBinding>() {

    private var zoomScale:Float=0f

    override fun initView() {
        binding!!.title.text= getString(R.string.video_record_configuration_zoom)
        setupProgress(zoomScale)
        binding!!.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setupToolTipText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
       binding!!.cancel.setOnClickListener{
           dismiss()
       }
        binding!!.oke.setOnClickListener {
            callback.onSelectZoomScale(zoomScale)
            dismiss()
        }
    }

    fun setupZoomScale(zoomScale: Float){
        this.zoomScale= zoomScale
    }

    private fun setupProgress(zoom: Float){
        Handler(Looper.getMainLooper()).postDelayed({
            setupToolTipText((zoom*100).toInt())
        }, 100)
    }

    private fun setupToolTipText(progress:Int){
        zoomScale= progress.toFloat()/100
        binding!!.txtProgress.text= zoomScale.toString()
        val position= (((binding!!.seekbar.right - binding!!.seekbar.left- 2*resources.getDimensionPixelSize(R.dimen.home_v_margin)).toFloat()/ binding!!.seekbar.max) * progress ).toInt()
        binding!!.txtProgress.x= position.toFloat()
    }

    override fun getLayout(): Int {
        return R.layout.layout_record_video_duration
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissCallback.onDismiss()
    }

    interface OnSelectZoomScale{
        fun onSelectZoomScale(zoom:Float)
    }
}