package com.hola360.backgroundvideorecoder.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.broadcastreciever.ListenRecordScheduleBroadcast
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.RecordSchedule
import com.hola360.backgroundvideorecoder.ui.record.video.ScheduleVideo
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object VideoRecordUtils {

    fun getCameraCapabilities(context: Context, lifeCycleOwner: LifecycleOwner): MutableList<CameraCapability>{
        val cameraProviderFuture= ProcessCameraProvider.getInstance(context)
        val provider= cameraProviderFuture.get()
        provider.unbindAll()
        val cameraCapabilities= mutableListOf<CameraCapability>()
        for (camSelector in arrayOf(
            CameraSelector.DEFAULT_BACK_CAMERA,
            CameraSelector.DEFAULT_FRONT_CAMERA
        )) {
            try {
                // just get the camera.cameraInfo to query capabilities
                // we are not binding anything here.
                if (provider.hasCamera(camSelector)) {
                    val camera = provider.bindToLifecycle(lifeCycleOwner, camSelector)
                    QualitySelector
                        .getSupportedQualities(camera.cameraInfo)
                        .filter { quality ->
                            listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD)
                                .contains(quality)
                        }.also {
                            cameraCapabilities.add(CameraCapability(camSelector, it))
                        }
                }
            } catch (exc: java.lang.Exception) {
                Log.e("CamException", "Camera Face $camSelector is not supported")
            }
        }
        return cameraCapabilities
    }

    fun getCameraQuality(isBack:Boolean, cameraCapabilities:MutableList<CameraCapability>):MutableList<String>{
        val cameraQualities= if(isBack){
            cameraCapabilities[0].qualities
        }else{
            cameraCapabilities[1].qualities
        }
        val qualitiesStrings= mutableListOf<String>()
        for(item in cameraQualities){
            qualitiesStrings.add(getStringQuality(item))
        }
        return qualitiesStrings
    }

    private fun getStringQuality(quality: Quality):String{
        return when(quality){
            Quality.SD->{
                "Low"
            }
            Quality.HD->{
                "Medium"
            }
            Quality.FHD->{
                "High"
            }
            Quality.UHD->{
                "UHD (3840x2160)"
            }
            else -> {""}
        }
    }

    fun Quality.getAspectRatioString(quality: Quality, portraitMode:Boolean) :String {
        val hdQualities = arrayOf(Quality.UHD, Quality.FHD, Quality.HD)
        val ratio =
            when {
                hdQualities.contains(quality) -> Pair(16, 9)
                quality == Quality.SD         -> Pair(4, 3)
                else -> throw UnsupportedOperationException()
            }

        return if (portraitMode) "V,${ratio.second}:${ratio.first}"
        else "H,${ratio.first}:${ratio.second}"
    }

    fun Quality.getAspectRatio(quality: Quality): Int {
        return when {
            arrayOf(Quality.UHD, Quality.FHD, Quality.HD)
                .contains(quality)   -> AspectRatio.RATIO_16_9
            (quality ==  Quality.SD) -> AspectRatio.RATIO_4_3
            else -> throw UnsupportedOperationException()
        }
    }

    fun bindRecordUserCase(context: Context, lifeCycleOwner: LifecycleOwner,
                           videoRecordConfiguration: VideoRecordConfiguration):VideoCapture<Recorder>? {
        val cameraCapabilities= getCameraCapabilities(context, lifeCycleOwner)
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val cameraIndex= if(videoRecordConfiguration.isBack){
            0
        }else{
            1
        }
        val cameraSelector = cameraCapabilities[cameraIndex].camSelector
        val quality: Quality = cameraCapabilities[cameraIndex].qualities[videoRecordConfiguration.backCameraQuality]
        val qualitySelector = QualitySelector.from(quality)

        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()

        val videoCapture = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifeCycleOwner,
                cameraSelector,
                videoCapture
            )
            return videoCapture
        } catch (exc: Exception) {
            // we are on main thread, let's reset the controls on the UI.
            Log.e("CameraTest", "Use case binding failed", exc)
        }
        return null
    }

    fun generateMediaStoreOutput(context: Context):MediaStoreOutputOptions{
        val name = "CameraX-recording-" +
                SimpleDateFormat(PreviewVideoWindow.FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        return MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
    }

    fun generateFileOutput(file:File):FileOutputOptions{
        return FileOutputOptions.Builder(file).build()
    }

    fun generateRecordTime(time:Long):String{
        val timeInSecond= time/1000
        val hour= timeInSecond/3600
        val minute= (timeInSecond%3600)/60
        val second= (timeInSecond%3600)%60
        return  String.format("%02d:%02d:%02d", hour, minute, second)
    }


    fun getVideoConfiguration(context: Context):VideoRecordConfiguration{
        val dataPref = DataSharePreferenceUtil.getInstance(context)
        return if (dataPref?.getVideoConfiguration() != "") {
            Gson().fromJson(
                dataPref!!.getVideoConfiguration(),
                VideoRecordConfiguration::class.java
            )
        } else {
            VideoRecordConfiguration()
        }
    }

    fun getVideoSchedule(context: Context):RecordSchedule{
        val dataPref = DataSharePreferenceUtil.getInstance(context)
        return if(dataPref?.getSchedule() != ""){
            val schedule= Gson().fromJson(dataPref?.getSchedule(), RecordSchedule::class.java)
            if(schedule.scheduleTime+ getVideoConfiguration(context).totalTime< System.currentTimeMillis()){
                RecordSchedule()
            }else{
                schedule
            }
        }else{
            RecordSchedule()
        }
    }

    fun generateScheduleTime(context: Context):String{
        val dataPref = DataSharePreferenceUtil.getInstance(context)
        val videoSchedule= Gson().fromJson(dataPref!!.getSchedule(), RecordSchedule::class.java)
        val dateFormat= SimpleDateFormat(BindingUtils.DATE_FORMAT, Locale.getDefault())
        val timeFormat= SimpleDateFormat(BindingUtils.TIME_FORMAT, Locale.getDefault())
        val time= timeFormat.format(videoSchedule.scheduleTime)
        val date= dateFormat.format(videoSchedule.scheduleTime)
        return time.plus("  $date")
    }

    fun startRecordIntent(context: Context, status:Int) {
        val intent=  Intent(context, RecordService::class.java).apply {
            putExtra(Constants.RECORD_VIDEO_TYPE, true)
            putExtra(Constants.VIDEO_STATUS, status)
        }
        context.startService(intent)
    }

    private fun getBroadcastPendingIntent(context: Context):PendingIntent{
        val intent=  Intent(context, ListenRecordScheduleBroadcast::class.java).apply {
            action = Constants.SCHEDULE_TYPE
            putExtra(Constants.SCHEDULE_TYPE, true)
        }
        return PendingIntent.getBroadcast(
            context, ScheduleVideo.BROADCAST_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun setAlarmSchedule(context: Context, time: Long){
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if(SystemUtils.isAndroidO()){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, getBroadcastPendingIntent(context))
        }else{
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, getBroadcastPendingIntent(context))
        }
    }

    fun cancelAlarmSchedule(context: Context){
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getBroadcastPendingIntent(context))
    }

    fun checkScheduleWhenRecordStop(context: Context){
        val schedule= getVideoSchedule(context)
        if(schedule.scheduleTime != 0L && schedule.scheduleTime<  System.currentTimeMillis()){
            val dataPref = DataSharePreferenceUtil.getInstance(context)
            dataPref!!.putSchedule("")
        }
    }
}