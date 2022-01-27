package com.hola360.backgroundvideorecoder.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.ui.dialog.PreviewVideoWindow
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability
import com.hola360.backgroundvideorecoder.ui.record.video.model.CustomLifeCycleOwner
import com.hola360.backgroundvideorecoder.ui.record.video.model.VideoRecordConfiguration
import java.lang.Exception
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
        val quality: Quality = cameraCapabilities[cameraIndex].qualities[videoRecordConfiguration.cameraQuality]
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
}