package com.hola360.backgroundvideorecoder.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.UriPermission
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.inSdCardStorage
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel
import java.io.File
import java.util.*
import kotlin.math.ln
import kotlin.math.pow


object Utils {

    fun isAndroidQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    fun isAndroidO(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun isAndroidR(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    fun getDocumentFile(context: Context, path: String): DocumentFile? {
        val documentFile = DocumentFile.fromFile(File(path))
        val isFromSdCard = documentFile.inSdCardStorage(context)
        return if (isFromSdCard) {
            val uriSdCard = SharedPreferenceUtils.getInstance(context)!!.getUriSdCard()
            val rootUriTree = Uri.parse(uriSdCard)
            var parentDocument =
                DocumentFile.fromTreeUri(context, rootUriTree!!)
            val parts: List<String> = path.split(Regex("/"))
            for (i in 3 until parts.size) {
                if (parentDocument != null) {
                    parentDocument = parentDocument.findFile(parts[i])
                }
            }
            parentDocument
        } else {
            documentFile
        }
    }

    fun isGrantAccessSdCard(context: Context): Boolean {
        val contentResolver = context.contentResolver
        val permissions: List<UriPermission> = contentResolver.persistedUriPermissions
        return permissions.isNotEmpty() && SharedPreferenceUtils.getInstance(context)!!
            .getUriSdCard()!!.isNotEmpty()
    }

    fun getDocumentationFolder(): File {
        val root = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)!!.path.plus(
                File.separator
            )
                .plus(Constants.FOLDER_NAME)
        )

        if (!root.exists()) {
            root.mkdirs()
        }
        return root
    }

    fun formatTimeIntervalHourMinSec(mills: Long): String? {
        val hour = mills / (60 * 60 * 1000)
        val min = mills / (60 * 1000) % 60
        val sec = mills / 1000 % 60
        return if (hour == 0L) {
            if (min == 0L) {
                String.format(Locale.getDefault(), "00:00:%02d", sec)
            } else {
                String.format(Locale.getDefault(), "00:%02d:%02d", min, sec)
            }
        } else {
            String.format(Locale.getDefault(), "%02dh:%02dm:%02ds", hour, min, sec)
        }
    }

    fun showInvalidateTime(view: View) {
        Snackbar.make(
            view,
            view.resources.getString(R.string.video_record_schedule_invalidate_time),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun getDataPrefGeneralSetting(dataPref: SharedPreferenceUtils): SettingGeneralModel {
        val value = dataPref.getGeneralSetting()
        value?.let {
            return if ("" == it) {
                SettingGeneralModel()
            } else {
                Gson().fromJson(it, SettingGeneralModel::class.java)
            }
        }
        return SettingGeneralModel()
    }

    @JvmStatic
    fun convertTime(time: Long): String {
        val result: String
        val hour = time / 3600
        val remainSecond = time % 3600
        val minutes = remainSecond / 60
        val second = remainSecond % 60
        result = if (hour > 0) {
            String.format("%02d:%02d:%02d", hour, minutes, second)
        } else {
            String.format("%02d:%02d", minutes, second)
        }
        return result
    }

    fun hasShowRequestPermissionRationale(
        context: Context?,
        vararg permissions: String?
    ): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (context as Activity?)!!,
                        permission!!
                    )
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun storagePermissionGrant(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Environment.isExternalStorageManager()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
            }
            else -> {
                (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED) and
                        (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED)
            }
        }
    }

    fun recordPermissionGrant(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun humanReadableByteCountBin(bytes: Long, factor: Float = 1024f): String {
        return if (bytes < factor) {
            "$bytes B"
        } else {
            val result: Int = (ln(bytes.toDouble()) / ln(factor)).toInt()
            String.format("%.2f%c", bytes / factor.pow(result), "KMGTPE"[result - 1])
                .replace(",", ".")
        }
    }

    fun adjustAudio(context: Context, setMute: Boolean) {
        if (setMute) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            val currentMode = audioManager?.ringerMode
            if (currentMode != AudioManager.RINGER_MODE_SILENT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val adJustMute: Int = if (setMute) {
                        AudioManager.ADJUST_MUTE
                    } else {
                        AudioManager.ADJUST_UNMUTE
                    }
                    audioManager!!.adjustStreamVolume(
                        AudioManager.STREAM_NOTIFICATION,
                        adJustMute,
                        0
                    )
                    audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, adJustMute, 0)
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, adJustMute, 0)
                    audioManager.adjustStreamVolume(AudioManager.STREAM_RING, adJustMute, 0)
                    audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, adJustMute, 0)
                } else {
                    audioManager!!.setStreamMute(AudioManager.STREAM_NOTIFICATION, setMute)
                    audioManager.setStreamMute(AudioManager.STREAM_ALARM, setMute)
                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, setMute)
                    audioManager.setStreamMute(AudioManager.STREAM_RING, setMute)
                    audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, setMute)
                }
            }
        }
    }

    fun getStoragePermissions(): Array<String> {
        return if (isAndroidQ()) {
            Constants.STORAGE_PERMISSION_STORAGE_SCOPE
        } else {
            Constants.STORAGE_PERMISSION_UNDER_STORAGE_SCOPE
        }
    }

}