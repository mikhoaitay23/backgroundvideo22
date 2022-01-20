package com.hola360.backgroundvideorecoder.data.model.audio

data class AudioModel(
    var quality: AudioQuality = AudioQuality.MEDIUM,
    var mode: AudioMode = AudioMode.MONO,
    var duration: Float? = null,
    var isMuted: Boolean = false
)
