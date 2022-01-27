package com.hola360.backgroundvideorecoder.data.model.audio

enum class AudioQuality {
    LOW, MEDIUM, HIGH;

    companion object {
        fun getByInt(value: Int) = values()[value]
        fun obtainQuality(audioQuality: AudioQuality): Float {
            return when (audioQuality) {
                LOW -> {
                    8000f
                }
                MEDIUM -> {
                    16000f
                }
                HIGH -> {
                    44100f
                }
            }
        }
    }
}