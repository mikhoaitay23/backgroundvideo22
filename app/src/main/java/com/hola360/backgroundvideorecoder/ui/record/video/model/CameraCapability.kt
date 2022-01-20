package com.hola360.backgroundvideorecoder.ui.record.video.model

import androidx.camera.core.CameraSelector
import androidx.camera.video.Quality

class CameraCapability(val camSelector: CameraSelector, val qualities:List<Quality>) {
}