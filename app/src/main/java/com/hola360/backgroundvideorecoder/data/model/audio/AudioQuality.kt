package com.hola360.backgroundvideorecoder.data.model.audio

enum class AudioQuality {
    LOW, MEDIUM, HIGH;

    companion object {
        fun getByInt(value: Int) = values()[value]
        fun obtainQuality(audioQuality: AudioQuality): Float {
            return when (audioQuality) {
                LOW -> {
                    0.3f
                }
                MEDIUM -> {
                    0.6f
                }
                HIGH -> {
                    0.75f
                }
            }
        }
    }
}