package com.hola360.backgroundvideorecoder.data.model.audio

enum class AudioMode {
    MONO, STEREO;

    companion object {
        fun getByInt(value: Int) = values()[value]
        fun obtainMode(audioMode: AudioMode): Int {
            return when (audioMode) {
                MONO -> {
                    1
                }
                STEREO -> {
                    2
                }
            }
        }
    }
}