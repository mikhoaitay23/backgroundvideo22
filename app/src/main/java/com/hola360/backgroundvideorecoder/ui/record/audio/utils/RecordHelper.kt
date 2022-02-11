package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.annotation.SuppressLint
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.Looper
import com.zlw.main.recorderlib.recorder.RecordConfig
import com.zlw.main.recorderlib.recorder.RecordHelper
import com.zlw.main.recorderlib.recorder.listener.*
import com.zlw.main.recorderlib.recorder.mp3.Mp3EncodeThread
import com.zlw.main.recorderlib.recorder.mp3.Mp3EncodeThread.ChangeBuffer
import com.zlw.main.recorderlib.recorder.wav.WavUtils
import com.zlw.main.recorderlib.utils.ByteUtils
import com.zlw.main.recorderlib.utils.FileUtils
import com.zlw.main.recorderlib.utils.Logger
import fftlib.FftFactory
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class RecordHelper {

    private val TAG = RecordHelper::class.java.simpleName

    private var currentState = RecordState.IDLE
    private val RECORD_AUDIO_BUFFER_TIMES = 1

    private var recordStateListener: RecordStateListener? = null
    private var recordDataListener: RecordDataListener? = null
    private var recordSoundSizeListener: RecordSoundSizeListener? = null
    private var recordResultListener: RecordResultListener? = null
    private var recordFftDataListener: RecordFftDataListener? = null
    private var currentConfig: RecordConfig? = null
    private var audioRecordThread: AudioRecordThread? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var resultFile: File? = null
    private var tmpFile: File? = null
    private val files: MutableList<File> = ArrayList()
    private var mp3EncodeThread: Mp3EncodeThread? = null

    fun getState(): RecordState? {
        return currentState
    }

    fun setRecordStateListener(recordStateListener: RecordStateListener?) {
        this.recordStateListener = recordStateListener
    }

    fun setRecordDataListener(recordDataListener: RecordDataListener?) {
        this.recordDataListener = recordDataListener
    }

    fun setRecordSoundSizeListener(recordSoundSizeListener: RecordSoundSizeListener?) {
        this.recordSoundSizeListener = recordSoundSizeListener
    }

    fun setRecordResultListener(recordResultListener: RecordResultListener?) {
        this.recordResultListener = recordResultListener
    }

    fun setRecordFftDataListener(recordFftDataListener: RecordFftDataListener?) {
        this.recordFftDataListener = recordFftDataListener
    }

    fun start(filePath: String?, config: RecordConfig?) {
        currentConfig = config
        if (currentState != RecordState.IDLE && currentState != RecordState.STOP) {
            Logger.e(TAG, "状态异常当前状态： %s", currentState.name)
            return
        }
        resultFile = File(filePath)
        val tempFilePath = getTempFilePath()
        Logger.d(
            TAG,
            "----------------开始录制 %s------------------------",
            currentConfig!!.format.name
        )
        Logger.d(TAG, "参数： %s", currentConfig.toString())
        Logger.i(TAG, "pcm缓存 tmpFile: %s", tempFilePath)
        Logger.i(TAG, "录音文件 resultFile: %s", filePath)
        tmpFile = File(tempFilePath)
        audioRecordThread = AudioRecordThread()
        audioRecordThread!!.start()
    }

    fun stop() {
        if (currentState == RecordState.IDLE) {
            Logger.e(TAG, "状态异常当前状态： %s", currentState.name)
            return
        }
        if (currentState == RecordState.PAUSE) {
            makeFile()
            currentState = RecordState.IDLE
            notifyState()
            stopMp3Encoded()
        } else {
            currentState = RecordState.STOP
            notifyState()
        }
    }

    fun pause() {
        if (currentState != RecordState.RECORDING) {
            Logger.e(TAG, "状态异常当前状态： %s", currentState.name)
            return
        }
        currentState = RecordState.PAUSE
        notifyState()
    }

    fun resume() {
        if (currentState != RecordState.PAUSE) {
            Logger.e(TAG, "状态异常当前状态： %s", currentState.name)
            return
        }
        val tempFilePath = getTempFilePath()
        Logger.i(TAG, "tmpPCM File: %s", tempFilePath)
        tmpFile = File(tempFilePath)
        audioRecordThread = AudioRecordThread()
        audioRecordThread!!.start()
    }

    private fun notifyState() {
        if (recordStateListener == null) {
            return
        }
        mainHandler.post {  }
        if (currentState == RecordState.STOP || currentState == RecordState.PAUSE) {
            if (recordSoundSizeListener != null) {
                recordSoundSizeListener!!.onSoundSize(0)
            }
        }
    }

    private fun notifyFinish() {
        Logger.d(TAG, "录音结束 file: %s", resultFile!!.absolutePath)
        mainHandler.post {
            if (recordStateListener != null) {

            }
            if (recordResultListener != null) {
                recordResultListener!!.onResult(resultFile)
            }
        }
    }

    private fun notifyError(error: String) {
        if (recordStateListener == null) {
            return
        }
        mainHandler.post { recordStateListener!!.onError(error) }
    }

    private val fftFactory = FftFactory(FftFactory.Level.Original)

    private fun notifyData(data: ByteArray) {
        if (recordDataListener == null && recordSoundSizeListener == null && recordFftDataListener == null) {
            return
        }
        mainHandler.post {
            if (recordDataListener != null) {
                recordDataListener!!.onData(data)
            }
            if (recordFftDataListener != null || recordSoundSizeListener != null) {
                val fftData = fftFactory.makeFftData(data)
                if (fftData != null) {
                    if (recordSoundSizeListener != null) {
                        recordSoundSizeListener!!.onSoundSize(getDb(fftData))
                    }
                    if (recordFftDataListener != null) {
                        recordFftDataListener!!.onFftData(fftData)
                    }
                }
            }
        }
    }

    private fun getDb(data: ByteArray): Int {
        var sum = 0.0
        val ave: Double
        val length = Math.min(data.size, 128)
        val offsetStart = 0
        for (i in offsetStart until length) {
            sum += (data[i] * data[i]).toDouble()
        }
        ave = sum / (length - offsetStart)
        return (Math.log10(ave) * 20).toInt()
    }

    private fun initMp3EncoderThread(bufferSize: Int) {
        try {
            mp3EncodeThread = Mp3EncodeThread(resultFile, bufferSize)
            mp3EncodeThread!!.start()
        } catch (e: Exception) {
            Logger.e(e, TAG, e.message)
        }
    }

    @SuppressLint("MissingPermission")
    private inner class AudioRecordThread() : Thread() {
        private val audioRecord: AudioRecord
        private val bufferSize: Int
        override fun run() {
            super.run()
            when (currentConfig!!.format) {
                RecordConfig.RecordFormat.MP3 -> startMp3Recorder()
                else -> startPcmRecorder()
            }
        }

        private fun startPcmRecorder() {
            currentState = RecordState.RECORDING
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tmpFile)
                audioRecord.startRecording()
                val byteBuffer = ByteArray(bufferSize)
                while (currentState == RecordState.RECORDING) {
                    val end = audioRecord.read(byteBuffer, 0, byteBuffer.size)
                    notifyData(byteBuffer)
                    fos!!.write(byteBuffer, 0, end)
                    fos.flush()
                }
                audioRecord.stop()
                files.add(tmpFile!!)
                if (currentState == RecordState.STOP) {
                    makeFile()
                } else {
                    Logger.i(TAG, "暂停！")
                }
            } catch (e: Exception) {
                Logger.e(e, TAG, e.message)
                notifyError("录音失败")
            } finally {
                try {
                    fos?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (currentState != RecordState.PAUSE) {
                currentState = RecordState.IDLE
                notifyState()
                Logger.d(TAG, "录音结束")
            }
        }

        private fun startMp3Recorder() {
            currentState = RecordState.RECORDING
            notifyState()
            try {
                audioRecord.startRecording()
                val byteBuffer = ShortArray(bufferSize)
                while (currentState == RecordState.RECORDING) {
                    val end = audioRecord.read(byteBuffer, 0, byteBuffer.size)
                    if (mp3EncodeThread != null) {
                        mp3EncodeThread!!.addChangeBuffer(ChangeBuffer(byteBuffer, end))
                    }
                    notifyData(ByteUtils.toBytes(byteBuffer))
                }
                audioRecord.stop()
            } catch (e: Exception) {
                Logger.e(e, TAG, e.message)
                notifyError("录音失败")
            }
            if (currentState != RecordState.PAUSE) {
                currentState = RecordState.IDLE
                notifyState()
                stopMp3Encoded()
            } else {
                Logger.d(TAG, "暂停")
            }
        }

        init {
            bufferSize = AudioRecord.getMinBufferSize(
                currentConfig!!.sampleRate,
                currentConfig!!.channelConfig, currentConfig!!.encodingConfig
            ) * RECORD_AUDIO_BUFFER_TIMES
            Logger.d(TAG, "record buffer size = %s", bufferSize)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, currentConfig!!.sampleRate,
                currentConfig!!.channelConfig, currentConfig!!.encodingConfig, bufferSize
            )
            if (currentConfig!!.format == RecordConfig.RecordFormat.MP3) {
                if (mp3EncodeThread == null) {
                    initMp3EncoderThread(bufferSize)
                } else {
                    Logger.e(TAG, "mp3EncodeThread != null, 请检查代码")
                }
            }
        }
    }

    private fun stopMp3Encoded() {
        if (mp3EncodeThread != null) {
            mp3EncodeThread!!.stopSafe {
                notifyFinish()
                mp3EncodeThread = null
            }
        } else {
            Logger.e(TAG, "mp3EncodeThread is null, 代码业务流程有误，请检查！！ ")
        }
    }

    private fun makeFile() {
        when (currentConfig!!.format) {
            RecordConfig.RecordFormat.MP3 -> return
            RecordConfig.RecordFormat.WAV -> {
                mergePcmFile()
                makeWav()
            }
            RecordConfig.RecordFormat.PCM -> mergePcmFile()
            else -> {}
        }
        notifyFinish()
        Logger.i(TAG, "录音完成！ path: %s ； 大小：%s", resultFile!!.absoluteFile, resultFile!!.length())
    }

    /**
     * 添加Wav头文件
     */
    private fun makeWav() {
        if (!FileUtils.isFile(resultFile) || resultFile!!.length() == 0L) {
            return
        }
        val header = WavUtils.generateWavFileHeader(
            resultFile!!.length().toInt(),
            currentConfig!!.sampleRate,
            currentConfig!!.channelCount,
            currentConfig!!.encoding
        )
        WavUtils.writeHeader(resultFile, header)
    }

    private fun mergePcmFile() {
        val mergeSuccess = mergePcmFiles(resultFile, files)
        if (!mergeSuccess) {
            notifyError("合并失败")
        }
    }

    private fun mergePcmFiles(recordFile: File?, files: MutableList<File>?): Boolean {
        if (recordFile == null || files == null || files.size <= 0) {
            return false
        }
        var fos: FileOutputStream? = null
        var outputStream: BufferedOutputStream? = null
        val buffer = ByteArray(1024)
        try {
            fos = FileOutputStream(recordFile)
            outputStream = BufferedOutputStream(fos)
            for (i in files.indices) {
                val inputStream = BufferedInputStream(FileInputStream(files[i]))
                var readCount: Int
                while (inputStream.read(buffer).also { readCount = it } > 0) {
                    outputStream.write(buffer, 0, readCount)
                }
                inputStream.close()
            }
        } catch (e: Exception) {
            Logger.e(e, TAG, e.message)
            return false
        } finally {
            try {
                outputStream?.close()
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        for (i in files.indices) {
            files[i].delete()
        }
        files.clear()
        return true
    }

    private fun getTempFilePath(): String {
        val fileDir = String.format(
            Locale.getDefault(),
            "%s/Record/",
            Environment.getExternalStorageDirectory().absolutePath
        )
        if (!FileUtils.createOrExistsDir(fileDir)) {
            Logger.e(TAG, "文件夹创建失败：%s", fileDir)
        }
        val fileName = String.format(
            Locale.getDefault(), "record_tmp_%s", FileUtils.getNowString(
                SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.SIMPLIFIED_CHINESE)
            )
        )
        return String.format(Locale.getDefault(), "%s%s.pcm", fileDir, fileName)
    }

    enum class RecordState {
        IDLE,
        RECORDING,
        PAUSE,
        STOP,
        FINISH
    }
}