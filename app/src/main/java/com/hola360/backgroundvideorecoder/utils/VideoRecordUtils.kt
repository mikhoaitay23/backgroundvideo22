package com.hola360.backgroundvideorecoder.utils

import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.fragment.app.FragmentActivity
import com.hola360.backgroundvideorecoder.ui.record.video.model.CameraCapability

object VideoRecordUtils {

    fun getCameraCapabilities(activity:FragmentActivity): MutableList<CameraCapability>{
        val cameraProviderFuture= ProcessCameraProvider.getInstance(activity)
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
                    val camera = provider.bindToLifecycle(activity, camSelector)
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
}