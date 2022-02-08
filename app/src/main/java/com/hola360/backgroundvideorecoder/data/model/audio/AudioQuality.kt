package com.hola360.backgroundvideorecoder.data.model.audio

enum class AudioQuality {
    HIGH, MEDIUM, LOW;

    companion object {
        fun getByInt(value: Int) = values()[value]
        fun obtainQuality(audioQuality: AudioQuality): Float {
            return when (audioQuality) {
                HIGH -> {
                    44100f
                }
                MEDIUM -> {
                    16000f
                }
                LOW -> {
                    8000f
                }
            }
        }
    }
}