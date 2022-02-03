package com.hola360.backgroundvideorecoder.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.hola360.backgroundvideorecoder.R

object BindingUtils {

    @BindingAdapter("videoRecordDuration")
    @JvmStatic
fun videoRecordDuration(textView: TextView, duration:Long?){
    duration?.let {
        textView.text= if(it>0){
            val time= it/1000/60
            textView.resources.getQuantityString(R.plurals.video_record_configuration_minute,
                time.toInt(), time)
        }else{
            textView.resources.getString(R.string.video_record_configuration_un_limit)
        }
    }
}

    @BindingAdapter("timePerVideo")
    @JvmStatic
    fun timePerVideo(textView: TextView, duration:Long?){
        duration?.let {
            textView.text= if(it>0){
                val time= it/1000/60
                textView.resources.getQuantityString(R.plurals.video_record_configuration_minute,
                    time.toInt(), time.toInt())
            }else{
                textView.resources.getString(R.string.video_record_configuration_no_split)
            }
        }
    }

}