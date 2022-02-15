package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.findFolder
import com.anggrayudi.storage.file.getAbsolutePath
import com.anggrayudi.storage.file.inSdCardStorage
import com.anggrayudi.storage.file.toRawFile
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.io.FileWritableAccessIO
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.io.FileWritableInSdCardAccessIO
import com.hola360.backgroundvideorecoder.utils.Configurations
import com.hola360.backgroundvideorecoder.utils.DataSharePreferenceUtil
import com.hola360.backgroundvideorecoder.utils.Utils
import com.naman14.androidlame.AndroidLame
import com.naman14.androidlame.LameBuilder
import java.io.*
import java.lang.Exception

class SoundRecorder(
    val context: Context,
    fileName: String?,
    val mSampleRate: Int,
    val onRecorderListener: OnRecorderListener?
) {

    private var mIsRecording = false
    private var mHandler: Handler? = null
    var minBufferSize = 0
    private var audioRecord: AudioRecord? = null
    private var androidLame: AndroidLame? = null
    private var outPutDocFile: DocumentFile? = null

    init {
        val parentPath = DataSharePreferenceUtil.getInstance(context)?.getParentPath()
        val rootParentDocFile = Utils.getDocumentFile(context, parentPath!!)
        if (rootParentDocFile != null && rootParentDocFile.exists()) {
            try {
                var parentRecordDocFile = rootParentDocFile.findFile(Configurations.RECORD_PATH)
                if (parentRecordDocFile == null || !parentRecordDocFile.exists()) {
                    parentRecordDocFile =
                        rootParentDocFile.createDirectory(Configurations.RECORD_PATH)
                }
                var audioFolderDoc =
                    parentRecordDocFile!!.findFolder(Configurations.RECORD_AUDIO_PATH)
                if (audioFolderDoc == null || !audioFolderDoc.exists()) {
                    audioFolderDoc =
                        parentRecordDocFile.createDirectory(Configurations.RECORD_AUDIO_PATH)
                }
                val mimeType = "audio/mp3"
                audioFolderDoc =
                    Utils.getDocumentFile(context, audioFolderDoc!!.getAbsolutePath(context))
                outPutDocFile = audioFolderDoc!!.createFile(mimeType, fileName!!)
            } catch (ex: Exception) {
                outPutDocFile = null
            }

        } else {
            outPutDocFile = null
        }

    }

    fun start() {
        if (mIsRecording) {
            return
        }
        if (outPutDocFile != null && outPutDocFile!!.exists()) {
            object : Thread() {
                override fun run() {
                    minBufferSize = AudioRecord.getMinBufferSize(
                        mSampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    if (minBufferSize < 0) {
                        if (mHandler != null) {
                            mHandler!!.sendEmptyMessage(MSG_ERROR_GET_MIN_BUFFER_SIZE)
                        }
                        return
                    }
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC, mSampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2
                    )

                    // PCM buffer size (5sec)
                    val buffer = ShortArray(mSampleRate * 2 * 5)
                    val mp3buffer = ByteArray((7200 + buffer.size * 2 * 1.25).toInt())

                    val outputFile = try {
                        if (outPutDocFile!!.inSdCardStorage(context)) {
                            if (!Utils.isGrantAccessSdCard(
                                    context
                                )
                            ) {
                                null
                            } else {
                                val docFile = Utils.getDocumentFile(
                                    context,
                                    outPutDocFile!!.getAbsolutePath(context)
                                )
                                FileWritableInSdCardAccessIO(
                                    context,
                                    docFile!!.uri
                                )
                            }
                        } else {
                            val mRaf =
                                RandomAccessFile(outPutDocFile!!.toRawFile(context), "rw")
                            FileWritableAccessIO(mRaf)
                        }
                    } catch (ex: Exception) {
                        if (mHandler != null) {
                            mHandler!!.sendEmptyMessage(MSG_ERROR_CREATE_FILE)
                        }
                        mIsRecording = false
                        return
                    }
                    androidLame = LameBuilder()
                        .setInSampleRate(mSampleRate)
                        .setOutChannels(1)
                        .setOutBitrate(OUTPUT_BITRATE)
                        .setOutSampleRate(mSampleRate).setScaleInput(5.0f)
                        .build()
                    mIsRecording = true
                    try {
                        try {
                            audioRecord!!.startRecording()
                        } catch (e: IllegalStateException) {
                            if (mHandler != null) {
                                mHandler!!.sendEmptyMessage(MSG_ERROR_REC_START)
                            }
                            return
                        }
                        try {
                            if (mHandler != null) {
                                mHandler!!.sendEmptyMessage(MSG_REC_STARTED)
                            }
                            var readSize = 0
                            while (mIsRecording) {
                                readSize = audioRecord!!.read(buffer, 0, minBufferSize)
                                onRecorderListener?.onBuffer(buffer, minBufferSize)
                                if (readSize < 0) {
                                    if (mHandler != null) {
                                        mHandler!!.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD)
                                    }
                                    break
                                } else if (readSize == 0) {
                                } else {
                                    val encResult: Int = androidLame!!.encode(
                                        buffer,
                                        buffer, readSize, mp3buffer
                                    )
                                    if (encResult < 0) {
                                        if (mHandler != null) {
                                            mHandler!!.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE)
                                        }
                                        break
                                    }
                                    if (encResult != 0) {
                                        try {
                                            outputFile!!.write(mp3buffer, 0, encResult)
                                        } catch (e: IOException) {
                                            if (mHandler != null) {
                                                mHandler!!.sendEmptyMessage(MSG_ERROR_WRITE_FILE)
                                            }
                                            break
                                        }
                                    }
                                }
                            }
                            val flushResult: Int = androidLame!!.flush(mp3buffer)
                            if (flushResult < 0) {
                                if (mHandler != null) {
                                    mHandler!!.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE)
                                }
                            }
                            if (flushResult > 0) {
                                try {
                                    outputFile!!.write(mp3buffer, 0, flushResult)
                                    outputFile.close()
                                } catch (e: IOException) {
                                    if (mHandler != null) {
                                        mHandler!!.sendEmptyMessage(MSG_ERROR_WRITE_FILE)
                                    }
                                }
                            }
                        } finally {
                            audioRecord!!.stop()
                            audioRecord!!.release()
                        }
                    } finally {
                        androidLame!!.close()
                        mIsRecording = false
                    }
                    if (mHandler != null) {
                        mHandler!!.sendEmptyMessage(MSG_REC_STOPPED)
                    }
                }
            }.start()
        } else {
            if (mHandler != null) {
                mHandler!!.sendEmptyMessage(MSG_ERROR_WRITE_FILE)
            }
        }

    }


    fun stop() {
        mIsRecording = false
    }

    fun isRecording(): Boolean {
        return mIsRecording
    }

    fun setHandle(handler: Handler?) {
        mHandler = handler
    }

    fun getSessionId(): Int {
        return if (audioRecord != null) {
            audioRecord!!.audioSessionId
        } else -1
    }

    companion object {
        const val OUTPUT_BITRATE = 32
        const val MSG_REC_STARTED = 0
        const val MSG_REC_STOPPED = 1
        const val MSG_ERROR_GET_MIN_BUFFER_SIZE = 2
        const val MSG_ERROR_CREATE_FILE = 3
        const val MSG_ERROR_REC_START = 4
        const val MSG_ERROR_AUDIO_RECORD = 5
        const val MSG_ERROR_AUDIO_ENCODE = 6
        const val MSG_ERROR_WRITE_FILE = 7
        const val MSG_ERROR_CLOSE_FILE = 8
    }

    interface OnRecorderListener {
        fun onBuffer(buf: ShortArray?, minBufferSize: Int)
    }

}