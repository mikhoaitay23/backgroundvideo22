package com.hola360.backgroundvideorecoder.ui.record.audio.utils.io

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class FileWritableInSdCardAccessIO(context: Context, treeUri: Uri) : BaseFileIO() {
    private var pfdOutput: ParcelFileDescriptor? = null
    private var fos: FileOutputStream? = null
    private var position: Long = 0

    init {
        pfdOutput = context.contentResolver.openFileDescriptor(treeUri, "rw")
        fos = FileOutputStream(pfdOutput!!.fileDescriptor)
    }

    override fun write(bytes: ByteArray, start: Int, len: Int) {
        try {
            val fch: FileChannel = fos!!.channel
            fch.position(position)
            fch.write(ByteBuffer.wrap(bytes,start,len))
            position = fch.position()

        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    override fun close() {
        val fch = fos?.channel
        fch?.close()
        fos?.close()
        pfdOutput!!.close()
    }
}