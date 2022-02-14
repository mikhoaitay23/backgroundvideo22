package com.hola360.backgroundvideorecoder.ui.record.video.model

import android.os.Parcel
import android.os.Parcelable
import com.hola360.backgroundvideorecoder.ui.dialog.RecordVideoDurationDialog

class VideoRecordConfiguration {
    var isBack:Boolean= true
    var videoOrientation:Int=0
    var frontCameraQuality:Int=0
    var backCameraQuality:Int=0
    var zoomScale:Float=0f
    var totalTime:Long=0
    var timePerVideo:Long= RecordVideoDurationDialog.TIME_SQUARE
    var previewMode:Boolean= true
    var flash:Boolean= false
    var sound:Boolean= true
    var scheduleTime:Long=0L

}