package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.UriPermission
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.os.Build
import android.os.Environment
import android.util.TypedValue
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anggrayudi.storage.file.StorageId

import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.data.model.StorageModel
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import kotlin.math.ln
import kotlin.math.pow

object FilePickerUtils {
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

    fun humanReadableByteCountBin(bytes: Long, factor: Float = 1024f): String {
        return if (bytes < factor) {
            "$bytes B"
        } else {
            val result: Int = (ln(bytes.toDouble()) / ln(factor)).toInt()
            String.format("%.2f%c", bytes / factor.pow(result), "KMGTPE"[result - 1])
                .replace(",", ".")
        }
    }

    fun getStorageName(context: Context, storageModel: StorageModel): String {
        return if (storageModel.storageId == StorageId.PRIMARY) {
            context.getString(R.string.internal_storage)
        } else {
            context.getString(R.string.sd_card)
        }
    }


    fun getStorageIcon(storageModel: StorageModel): Int {
        return if (storageModel.storageId == StorageId.PRIMARY) {
            R.drawable.ic_phone
        } else {
            R.drawable.ic_sd
        }
    }

    fun isGrantAccessSdCard(context: Context): Boolean {
        val contentResolver = context.contentResolver
        val permissions: List<UriPermission> = contentResolver.persistedUriPermissions
        return permissions.isNotEmpty() && SharedPreferenceUtils.getInstance(context)!!.getUriSdCard()!!.isNotEmpty()
    }

    fun fetchAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
            context.obtainStyledAttributes(typedValue.data, intArrayOf(android.R.attr.colorAccent))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    fun fetchPrimaryTextColor(context: Context): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
            context.obtainStyledAttributes(
                typedValue.data,
                intArrayOf(android.R.attr.textColorPrimary)
            )
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    fun updateSelectImage(imageView: ImageView, isSelected: Boolean) {
        if (isSelected) {
            imageView.setColorFilter(
                fetchAccentColor(imageView.context),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            imageView.setColorFilter(
                fetchPrimaryTextColor(imageView.context),
                PorterDuff.Mode.SRC_IN
            )
        }
    }



    fun isAndroidR(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }
}