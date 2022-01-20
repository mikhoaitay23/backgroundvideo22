package com.hola360.backgroundvideorecoder.data.model.audio

import android.media.AudioFormat

enum class AudioMode {
    MONO, STEREO;

    companion object {
        fun getByInt(value: Int) = values()[value]
        fun obtainMode(audioMode: AudioMode): Int {
            return when (audioMode) {
                MONO -> {
                    AudioFormat.CHANNEL_IN_MONO
                }
                STEREO -> {
                    AudioFormat.CHANNEL_IN_STEREO
                }
            }
        }
    }
}