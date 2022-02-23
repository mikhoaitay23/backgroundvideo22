package com.hola360.backgroundvideorecoder.utils

import android.graphics.PorterDuff
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import com.anggrayudi.storage.file.mimeType
import com.bumptech.glide.Glide
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.model.StorageBrowserModel
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.mediafile.MediaFile
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.model.ActionModel
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils.updateSelectImage
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.StorageUtils
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.ThumbnailUtil
import java.io.File
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

    @BindingAdapter("videoRotate")
    @JvmStatic
    fun videoRotate(textView: TextView, rotation: Int?) {
        rotation?.let {
            val rotateStrings = textView.resources.getStringArray(R.array.camera_orientation)
            textView.text = rotateStrings[it]
        }
    }

    @BindingAdapter("videoZoomScale")
    @JvmStatic
    fun videoZoomScale(textView: TextView, zoomScale: Float?) {
        zoomScale?.let {
            textView.text = it.toString()
        }
    }

    @BindingAdapter("android:setScheduleDate")
    @JvmStatic
    fun setScheduleDate(textView: TextView, time: Long?) {
        time?.let {
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val txtTime = if (it == 0L) {
                dateFormat.format(Calendar.getInstance().timeInMillis)
            } else {
                dateFormat.format(it)
            }
            textView.text = txtTime
        }
    }

    @BindingAdapter("android:setScheduleTime")
    @JvmStatic
    fun setScheduleTime(textView: TextView, time: Long?) {
        time?.let {
            val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
            val txtTime = if (it == 0L) {
                timeFormat.format(Calendar.getInstance().timeInMillis)
            } else {
                timeFormat.format(it)
            }
            textView.text = txtTime
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @JvmStatic
    @BindingAdapter("android:pagerIconTint")
    fun pagerIconTint(imageView: ImageView, isSelected: Boolean) {
        val color = if (isSelected) {
            imageView.context.resources.getColor(R.color.md_white_1000, null)
        } else {
            imageView.context.resources.getColor(R.color.bg_page_un_select, null)
        }
        imageView.setColorFilter(color)
    }

    const val DATE_FORMAT = "dd/MM/yyyy"
    const val TIME_FORMAT = "HH:mm"

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
                val time = it / 1000
                textView.resources.getQuantityString(
                    R.plurals.video_record_configuration_minute,
                    time.toInt(), time.toInt()
                )
            } else {
                textView.resources.getString(R.string.video_record_configuration_un_limit)
            }
        }
    }

    @BindingAdapter("android:notificationLevel")
    @JvmStatic
    fun notificationLevel(textView: TextView, level: Int?) {
        level?.let {
            val listLevel = textView.resources.getStringArray(R.array.notification_importance)
            val txtImportance = if (level == 0) {
                listLevel[0]
            } else {
                ;listLevel[1]
            }
            textView.text = txtImportance
        }
    }

    @BindingAdapter("android:freeStorage")
    @JvmStatic
    fun freeStorage(textView: TextView, storageId: String?) {
        storageId?.let {
            val free = StorageUtils.getFree(textView.context, storageId)
            val total = StorageUtils.getTotal(textView.context, storageId)
            val freeStr = SystemUtils.formatSize(free)
            val totalStr = SystemUtils.formatSize(total)
            textView.text = String.format(
                textView.resources.getString(R.string.setting_general_free_pattern),
                freeStr, totalStr
            )
        }
    }

    @BindingAdapter("android:bindThumbnailFile")
    @JvmStatic
    fun ImageView.bindThumbnailFile(cFile: File) {
        val defaultThumb = ThumbnailUtil.getDefaultThumbnail(cFile)
        Glide.with(this).load(defaultThumb).into(this)
    }

    @BindingAdapter("android:iconForAction")
    @JvmStatic
    fun ImageView.iconForAction(actionModel: ActionModel) {
        if (actionModel.icon == -1) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
            setImageResource(actionModel.icon)
        }
        if (actionModel.isSelectable) {
            updateSelectImage(this, actionModel.isSelected)
        } else {
            updateSelectImage(this, false)
        }
    }

    @BindingAdapter("android:storageThumb")
    @JvmStatic
    fun ImageView.storageThumb(storageBrowserModel: StorageBrowserModel?) {
        if (storageBrowserModel != null) {
            setImageResource(
                FilePickerUtils.getStorageIcon(storageBrowserModel.curStorageModel())
            )
        } else {
            setImageResource(R.drawable.ic_phone)
        }
    }

    @BindingAdapter("android:appLockIndicatorOne")
    @JvmStatic
    fun appLockIndicatorOne(imageView: ImageView, indicatorNumber: Int) {
        val srcId = if ((indicatorNumber in 1..4) || (indicatorNumber in 6..9)) {
            R.drawable.app_lock_item_select
        } else {
            R.drawable.app_lock_item_unselect
        }
        imageView.setImageResource(srcId)
    }

    @BindingAdapter("android:appLockIndicatorTwo")
    @JvmStatic
    fun appLockIndicatorTwo(imageView: ImageView, indicatorNumber: Int) {
        val srcId = if ((indicatorNumber in 2..4) || (indicatorNumber in 7..9)) {
            R.drawable.app_lock_item_select
        } else {
            R.drawable.app_lock_item_unselect
        }
        imageView.setImageResource(srcId)
    }

    @BindingAdapter("android:appLockIndicatorThree")
    @JvmStatic
    fun appLockIndicatorThree(imageView: ImageView, indicatorNumber: Int) {
        val srcId = if ((indicatorNumber in 3..4) || (indicatorNumber in 8..9)) {
            R.drawable.app_lock_item_select
        } else {
            R.drawable.app_lock_item_unselect
        }
        imageView.setImageResource(srcId)
    }

    @BindingAdapter("android:appLockIndicatorFour")
    @JvmStatic
    fun appLockIndicatorFour(imageView: ImageView, indicatorNumber: Int) {
        val srcId = if ((indicatorNumber == 4) || (indicatorNumber == 9)) {
            R.drawable.app_lock_item_select
        } else {
            R.drawable.app_lock_item_unselect
        }
        imageView.setImageResource(srcId)
    }

    @BindingAdapter("android:setMediaThumb")
    @JvmStatic
    fun setMediaThumb(img: ImageView, item: MediaFile?) {
        if (item == null) {
            img.setImageResource(R.drawable.img_default_photo)
        } else {
            if (item.file.mimeType == img.context.getString(R.string.audio_mime_type)) {
                img.setImageResource(R.drawable.img_default_photo)
            } else {
                Glide.with(img.context).load(item.file.absolutePath)
                    .placeholder(R.drawable.img_default_photo)
                    .error(R.drawable.img_default_photo).into(img)
            }
        }
    }

    @BindingAdapter("android:setMediaDuration")
    @JvmStatic
    fun setMediaDuration(textView: TextView, item: MediaFile?) {
        textView.text = textView.context.getString(
            R.string.duration_size_my_file, Utils.getDuration(item!!.file),
            Utils.getFileSize(item.file)
        )
    }

    @BindingAdapter("android:setMediaDateTime")
    @JvmStatic
    fun setMediaDateTime(textView: TextView, item: MediaFile?) {
        textView.text = DateTimeUtils.getTimeDateMyFile(item!!.file.lastModified())
    }

    @BindingAdapter("android:setBackButton")
    @JvmStatic
    fun setBackButton(imageView: ImageView, isSelectAll: Boolean) {
        if (isSelectAll){
            Glide.with(imageView.context).load(R.drawable.ic_close).into(imageView)
        } else {
            Glide.with(imageView.context).load(R.drawable.ic_back).into(imageView)
        }
    }

}