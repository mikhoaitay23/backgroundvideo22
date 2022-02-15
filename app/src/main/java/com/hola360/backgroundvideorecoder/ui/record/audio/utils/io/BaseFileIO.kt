package com.hola360.backgroundvideorecoder.ui.record.audio.utils.io

abstract class BaseFileIO {
    abstract fun write(bytes: ByteArray, start: Int, len: Int)
    abstract fun close()
}