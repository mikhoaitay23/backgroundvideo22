package com.hola360.backgroundvideorecoder.ui.dialog

import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutRecordVideoDurationBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog

class RecordVideoDurationDialog(val callback:OnSelectDuration,
                                private val dismissCallback:OnDialogDismiss):BaseBottomSheetDialog<LayoutRecordVideoDurationBinding>() {

    private var totalTime:Long=0

    override fun initView() {
        binding!!.seekbar.max= SEEKBAR_PROGRESS
        setupProgress(totalTime)
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
            val duration= if(binding!!.seekbar.progress> TIME_SEGMENT){
                0L
            }else{
                ((binding!!.seekbar.progress.toFloat()/308*120).toInt()+1)*60000L
            }
            callback.onSelectDuration(duration)
            dismiss()
        }
    }

    fun setupTotalTime(totalTime: Long){
        this.totalTime= totalTime
    }

    private fun setupProgress(totalTime: Long){
        val progress= if(totalTime==0L){
            SEEKBAR_PROGRESS-6
        }else{
            (totalTime.toFloat()/60000/120* TIME_SEGMENT).toInt()
        }
        binding!!.seekbar.progress= progress
        Handler(Looper.getMainLooper()).postDelayed({
            setupToolTipText(progress)
        }, 100)

    }

    private fun setupToolTipText(progress:Int){
        val time= if(progress<= TIME_SEGMENT){
            ((progress.toFloat()/ TIME_SEGMENT* MAX_TIME).toInt()+1).toString()
        }else{
            resources.getString(R.string.video_record_configuration_un_limit)
        }
        binding!!.txtProgress.text= time
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

    companion object{
        const val SEEKBAR_PROGRESS=320
        const val TIME_SEGMENT= SEEKBAR_PROGRESS- 13+1
        const val MAX_TIME=120
    }

    interface OnSelectDuration{
        fun onSelectDuration(duration:Long)
    }
}