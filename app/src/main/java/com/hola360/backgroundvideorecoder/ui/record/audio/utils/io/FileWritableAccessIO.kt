package com.hola360.backgroundvideorecoder.ui.record.audio.utils.io

import java.io.IOException
import java.io.RandomAccessFile

class FileWritableAccessIO(private val mRandomAccessFile : RandomAccessFile) : BaseFileIO() {
    override fun write(bytes: ByteArray, start: Int, len: Int) {
        return try {
            mRandomAccessFile.write(bytes,start, len)
        } catch (exception: IOException) {
            throw IOException("Error reading random access file", exception)
        }
    }

    override fun close() {
        mRandomAccessFile.close()
    }
}