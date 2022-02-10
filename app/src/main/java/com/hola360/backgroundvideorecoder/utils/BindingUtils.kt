package com.hola360.backgroundvideorecoder.utils

import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.BindingAdapter
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog
import java.text.SimpleDateFormat
import java.util.*

object BindingUtils {

    @BindingAdapter("videoRecordDuration")
    @JvmStatic
    fun videoRecordDuration(textView: TextView, duration: Long?) {
        duration?.let {
            textView.text = if (it > 0) {
                val time = it / RecordVideoDurationDialog.TIME_SQUARE
                textView.resources.getQuantityString(
                    R.plurals.video_record_configuration_minute,
                    time.toInt(), time
                )
            } else {
                textView.resources.getString(R.string.video_record_configuration_un_limit)
            }
        }
    }

    @BindingAdapter("timePerVideo")
    @JvmStatic
    fun timePerVideo(textView: TextView, duration: Long?) {
        duration?.let {
            textView.text = if (it > 0) {
                val time = it / RecordVideoDurationDialog.TIME_SQUARE
                textView.resources.getQuantityString(
                    R.plurals.video_record_configuration_minute,
                    time.toInt(), time.toInt()
                )
            } else {
                textView.resources.getString(R.string.video_record_configuration_no_split)
            }
        }
    }

    @BindingAdapter("android:setScheduleDate")
    @JvmStatic
    fun setScheduleDate(textView: TextView, time:Long?) {
        time?.let {
            val dateFormat= SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val txtTime= if(it==0L){
                dateFormat.format(Calendar.getInstance().timeInMillis)
            }else{
                dateFormat.format(it)
            }
            textView.text= txtTime
        }
    }

    @BindingAdapter("android:setScheduleTime")
    @JvmStatic
    fun setScheduleTime(textView: TextView, time:Long?) {
        time?.let {
            val timeFormat= SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
            val txtTime= if(it==0L){
                timeFormat.format(Calendar.getInstance().timeInMillis)
            }else{
                timeFormat.format(it)
            }
            textView.text= txtTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @JvmStatic
    @BindingAdapter("android:pagerIconTint")
    fun pagerIconTint(imageView: ImageView, isSelected: Boolean) {
        val color= if(isSelected){
            imageView.context.resources.getColor(R.color.md_white_1000, null)
        }else{
            imageView.context.resources.getColor(R.color.bg_page_un_select, null)
        }
        imageView.setColorFilter(color)
    }

    const val DATE_FORMAT= "dd/MM/yyyy"
    const val TIME_FORMAT= "HH:mm"

    //Audio
    @BindingAdapter("android:setRecordQuality")
    @JvmStatic
    fun setRecordQuality(textView: TextView, audioModel: AudioModel?) {
        if (audioModel != null) {
            val recordQuality = textView.context.resources.getStringArray(R.array.record_quality)
            textView.text = recordQuality[audioModel.quality.ordinal]
        }
    }

    @BindingAdapter("android:setRecordMode")
    @JvmStatic
    fun setRecordMode(textView: TextView, audioModel: AudioModel?) {
        if (audioModel != null) {
            val recordMode = textView.context.resources.getStringArray(R.array.record_mode)
            textView.text = recordMode[audioModel.mode.ordinal]
        }
    }

    @BindingAdapter("android:setRecordAudioDuration")
    @JvmStatic
    fun setRecordAudioDuration(textView: TextView, audioModel: AudioModel?) {
        audioModel?.duration?.let {
            textView.text = if (it > 0) {
                val time = it / 1000 / 60
                textView.resources.getQuantityString(
                    R.plurals.video_record_configuration_minute,
                    time.toInt(), time.toInt()
                )
            } else {
                textView.resources.getString(R.string.video_record_configuration_un_limit)
            }
        }
    }

}