package com.hola360.backgroundvideorecoder.utils

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.Surface
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.res.ResourcesCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleOwner
import com.anggrayudi.storage.file.findFolder
import com.anggrayudi.storage.file.getAbsolutePath
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.RecordSchedule
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.hola360.backgroundvideorecoder.R

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

    fun generateRecordTime(time: Long):String{
        val timeInSecond= time/1000
        val hour= timeInSecond/3600
        val minute= (timeInSecond%3600)/60
        val second= (timeInSecond%3600)%60
        return String.format(" %02d:%02d:%02d", hour, minute, second)
    }

    fun getVideoConfiguration(context: Context):VideoRecordConfiguration{
        val dataPref = SharedPreferenceUtils.getInstance(context)
        return if (dataPref?.getVideoConfiguration() != "") {
            Gson().fromJson(
                dataPref!!.getVideoConfiguration(),
                VideoRecordConfiguration::class.java
            )
        } else {
            VideoRecordConfiguration()
        }
    }

    fun getSettingGeneralModel(context: Context):SettingGeneralModel{
        val dataPref= SharedPreferenceUtils.getInstance(context)
        dataPref?.let {
            val value= it.getGeneralSetting() ?: ""
            return if(value != ""){
                Gson().fromJson(value, SettingGeneralModel::class.java)
            }else{
                SettingGeneralModel()
            }
        }
        return SettingGeneralModel()
    }

    fun getVideoSchedule(context: Context):RecordSchedule{
        val dataPref = SharedPreferenceUtils.getInstance(context)
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

    fun checkScheduleWhenRecordStop(context: Context){
        val schedule= getVideoSchedule(context)
        if(schedule.scheduleTime != 0L && schedule.scheduleTime<  System.currentTimeMillis()){
            val dataPref = SharedPreferenceUtils.getInstance(context)
            dataPref!!.putSchedule("")
        }
    }

    fun getVideoRotation(context: Context, orientationAngle: Int):Int{
        return when(getVideoConfiguration(context).videoOrientation){
            0->{
                Surface.ROTATION_0
            }
            1->{
                if(isPortrait(orientationAngle)){
                    Surface.ROTATION_0
                }else{
                    Surface.ROTATION_90
                }
            }
            else->{
                if(isPortrait(orientationAngle)){
                    Surface.ROTATION_270
                }else{
                    Surface.ROTATION_0
                }
            }
        }
    }

    private fun isPortrait(orientationAngle:Int):Boolean{
        return !((orientationAngle in 46..134) || (orientationAngle in 226..314))
    }

    fun generateOutputFilepath(context: Context, videoFileName:String): DocumentFile?{
        val parentPath = SharedPreferenceUtils.getInstance(context)?.getParentPath()
        val rootParentDocFile = Utils.getDocumentFile(context, parentPath!!)
        return if (rootParentDocFile != null && rootParentDocFile.exists()) {
            try {
                var parentRecordDocFile = rootParentDocFile.findFile(Configurations.RECORD_PATH)
                if (parentRecordDocFile == null || !parentRecordDocFile.exists()) {
                    parentRecordDocFile =
                        rootParentDocFile.createDirectory(Configurations.RECORD_PATH)
                }
                var videoFolderDoc =
                    parentRecordDocFile!!.findFolder(Configurations.RECORD_VIDEO_PATH)
                if (videoFolderDoc == null || !videoFolderDoc.exists()) {
                    videoFolderDoc =
                        parentRecordDocFile.createDirectory(Configurations.RECORD_VIDEO_PATH)
                }
                val mimeType = "video/mp4"
                videoFolderDoc =
                    Utils.getDocumentFile(context, videoFolderDoc!!.getAbsolutePath(context))
                videoFolderDoc!!.createFile(mimeType, videoFileName)
            } catch (ex: Exception) {
                null
            }
        } else {
            null
        }
    }
}