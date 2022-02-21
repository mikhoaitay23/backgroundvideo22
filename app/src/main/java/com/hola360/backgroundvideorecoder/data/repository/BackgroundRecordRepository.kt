package com.hola360.backgroundvideorecoder.data.repository

import android.app.Application
import android.net.Uri
import com.hola360.backgroundvideorecoder.data.model.media.AudioFile
import com.hola360.backgroundvideorecoder.utils.Loader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackgroundRecordRepository(val application: Application) {

    init {

    }

    suspend fun getAllAudio(): MutableList<AudioFile> = withContext(Dispatchers.IO) {
        Loader.getAudios(application)
    }
}