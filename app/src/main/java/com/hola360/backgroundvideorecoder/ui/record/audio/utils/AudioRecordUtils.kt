package com.hola360.backgroundvideorecoder.ui.record.audio.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.video.MediaStoreOutputOptions
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import java.io.File
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordUtils {

    private var recorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var isPaused: Boolean = false
    private var updateTime: Long = 0
    private var durationMills: Long = 0
    private var recordFile: File? = null
    var recorderContract: RecorderContract? = null

    fun onStartRecording(context: Context, audioModel: AudioModel) {
        recorder = MediaRecorder()
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.DD.hh.mm.ss")
        val date = simpleDateFormat.format(Date())
        val outputFile = "/bg_audio_$date"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, outputFile)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()
        if (mediaStoreOutput != null) {
            recorder!!.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(AudioMode.obtainMode(audioModel.mode))
                setAudioSamplingRate(AudioQuality.obtainQuality(audioModel.quality).toInt())
                setMaxDuration(audioModel.duration) //Duration unlimited use RECORD_MAX_DURATION or -1
//                setOutputFile(mediaStoreOutput)
            }
            try {
                recorder!!.prepare()
                recorder!!.start()
                updateTime = System.currentTimeMillis()
                isRecording = true
                scheduleRecordingTimeUpdate()
                recorderContract?.onStartRecord(audioModel)
                isPaused = false
            } catch (e: Exception) {

            }
        } else {

        }

    }

    fun onResumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isPaused) {
            try {
                recorder!!.resume()
                updateTime = System.currentTimeMillis()
                scheduleRecordingTimeUpdate()
                if (recorderContract != null) {
                    recorderContract!!.onResumeRecord()
                }
                isPaused = false
            } catch (e: IllegalStateException) {

            }
        }
    }

    fun onPauseRecording() {
        if (isRecording) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!isPaused) {
                    try {
                        recorder!!.pause()
                        durationMills += System.currentTimeMillis() - updateTime
                        pauseRecordingTimer()
                        if (recorderContract != null) {
                            recorderContract!!.onPauseRecord()
                        }
                        isPaused = true
                    } catch (e: IllegalStateException) {

                    }
                }
            } else {
                onStopRecording()
            }
        }
    }

    fun onStopRecording() {
        if (isRecording) {
            stopRecordingTimer()
            try {
                recorder!!.stop()
            } catch (e: RuntimeException) {

            }
            recorder!!.release()
            if (recorderContract != null) {
                recorderContract!!.onStopRecord(recordFile)
            }
            durationMills = 0
            recordFile = null
            isRecording = false
            isPaused = false
            recorder = null
        } else {

        }
    }

    fun isRecording() = isRecording

    fun isPaused() = isPaused

    private fun stopRecordingTimer() {
        updateTime = 0
    }

    private fun pauseRecordingTimer() {
        updateTime = 0
    }

    private fun scheduleRecordingTimeUpdate() {

    }


    /**
     * Create file.
     * If it is not exists, than create it.
     * @param path Path to file.
     * @param fileName File name.
     */
    fun createFile(path: File?, fileName: String): File? {
        return if (path != null) {
            createDir(path)
            val file = File(path, fileName)
            //Create file if need.
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        Log.i(
                            "FileUtil.LOG_TAG",
                            "The file was successfully created! - " + file.absolutePath
                        )
                    } else {
                        Log.i("FileUtil.LOG_TAG", "The file exist! - " + file.absolutePath)
                    }
                } catch (e: IOException) {
                    Log.e("FileUtil.LOG_TAG", "Failed to create the file.", e)
                    return null
                }
            } else {
                Log.e("FileUtil.LOG_TAG", "File already exists!! Please rename file!")
                Log.i("FileUtil.LOG_TAG", "Renaming file")
                //				TODO: Find better way to rename file.
                return createFile(path, "1$fileName")
            }
            if (!file.canWrite()) {
                Log.e("FileUtil.LOG_TAG", "The file can not be written.")
            }
            file
        } else {
            null
        }
    }

    fun createDir(dir: File?): File? {
        if (dir != null) {
            if (!dir.exists()) {
                try {
                    if (dir.mkdirs()) {
                        Log.d("FileUtil.LOG_TAG", "Dirs are successfully created")
                        return dir
                    } else {
                        Log.e(
                            "FileUtil.LOG_TAG",
                            "Dirs are NOT created! Please check permission write to external storage!"
                        )
                    }
                } catch (e: java.lang.Exception) {
//                    Timber.e(e)
                }
            } else {
                Log.d("FileUtil.LOG_TAG", "Dir already exists")
                return dir
            }
        }
        Log.e("FileUtil.LOG_TAG", "File is null or unable to create dirs")
        return null
    }

}