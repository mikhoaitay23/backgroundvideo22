package com.hola360.backgroundvideorecoder.utils

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.UriPermission
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.inSdCardStorage
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.mediafile.MediaFile
import com.hola360.backgroundvideorecoder.ui.setting.model.SettingGeneralModel
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow
import androidx.core.content.FileProvider
import com.hola360.backgroundvideorecoder.BuildConfig
import com.hola360.backgroundvideorecoder.data.model.popup.PopupModel


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

    fun groupDataIntoHashMap(
        context: Context,
        files: List<MediaFile>
    ): LinkedHashMap<String, MutableSet<MediaFile>?>? {
        val groupedHashMap: LinkedHashMap<String, MutableSet<MediaFile>?> = LinkedHashMap()
        var list: MutableSet<MediaFile>?
        for (file in files) {
            val mDate =
                if (DateTimeUtils.getDateMyFileFlag(Calendar.getInstance().timeInMillis) == DateTimeUtils.getDateMyFileFlag(
                        file.file.lastModified()
                    )
                ) {
                    context.getString(R.string.today)
                } else {
                    DateTimeUtils.getDateMyFileFlag(file.file.lastModified())
                }
            if (groupedHashMap.containsKey(mDate)) {
                groupedHashMap[mDate]!!.add(file)
            } else {
                // The key is not there in the HashMap; create a new key-value pair
                list = LinkedHashSet()
                list.add(file)
                groupedHashMap[mDate.toString()] = list
            }
        }
        //Generate list from map
        return groupedHashMap
    }

    fun getDuration(file: File): String? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(file.absolutePath)
        val durationStr =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return formatMilliSecond(durationStr!!.toLong())
    }

    private fun formatMilliSecond(milliseconds: Long): String? {
        var finalTimerString = ""
        var secondsString = ""

        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        return finalTimerString
    }

    fun getFileSize(file: File): String {
        val df = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        df.applyPattern("0.00")

        val sizeKb = 1024.0f
        val sizeMb = sizeKb * sizeKb
        val sizeGb = sizeMb * sizeKb
        val sizeTerra = sizeGb * sizeKb


        return when {
            file.length() < sizeMb -> df.format(file.length() / sizeKb)
                .toString() + " Kb"
            file.length() < sizeGb -> df.format(file.length() / sizeMb)
                .toString() + " Mb"
            file.length() < sizeTerra -> df.format(file.length() / sizeGb)
                .toString() + " Gb"
            else -> ""
        }

    }

    fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
        val result: String?

        if (isAndroidQ()) {
            result = PathUtils.getPath(context, contentURI)
        } else {
            val cursor: Cursor = context.contentResolver.query(contentURI, null, null, null, null)!!
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    fun deletePdfFile(
        context: Context,
        uri: Uri
    ): Boolean {
        val path = getRealPathFromURI(context, uri)
        val file = File(path!!)
        val deleted = context.contentResolver.delete(
            uri,
            null,
            null
        ) > 0
        return deleted || file.delete()
    }

    fun openMp4File(context: Context, file: File) {
        val mimeType = "video/mp4"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val data = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            intent.setDataAndType(data, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            ToastUtils.getInstance(context)
                ?.showToast(context.getString(R.string.cannot_open_mp4))
        }
    }

    fun getPopupItems(context: Context): MutableList<PopupModel> {
        val popupItems = mutableListOf<PopupModel>()
        popupItems.add(
            PopupModel(R.drawable.ic_check, context.getString(R.string.select))
        )
        popupItems.add(
            PopupModel(R.drawable.ic_open_with, context.getString(R.string.open_with))
        )
        popupItems.add(
            PopupModel(R.drawable.ic_rename, context.getString(R.string.rename))
        )
        popupItems.add(
            PopupModel(R.drawable.ic_delete, context.getString(R.string.delete))
        )
        popupItems.add(
            PopupModel(R.drawable.ic_share, context.getString(R.string.share))
        )
        return popupItems
    }
}