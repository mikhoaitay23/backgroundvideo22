package com.hola360.backgroundvideorecoder.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Parcelable
import android.os.StatFs
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.ViewDataBinding
import androidx.exifinterface.media.ExifInterface
import com.google.android.material.snackbar.Snackbar
import com.hola360.backgroundvideorecoder.R
import java.io.*
import java.text.SimpleDateFormat

object SystemUtils {
    private const val FULL_DATE_TIME = "MMMM dd, yyyy, HH:mm"
    private const val MSG_TIME_ONLY = "HH:mm"

    @JvmStatic
    fun getResUrl(resId: Int): String {
        return "android.resource://com.hola360.qrcodescan/$resId"
    }

    @JvmStatic
    fun getFullDateTime(date: Long): String {
        val sdfDate = SimpleDateFormat(FULL_DATE_TIME)
        return sdfDate.format(date)
    }

    @JvmStatic
    fun getTimeOnLy(date: Long): String {
        val sdfDate = SimpleDateFormat(MSG_TIME_ONLY)
        return sdfDate.format(date)
    }

    @JvmStatic
    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                val notGiant = ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                ) != PackageManager.PERMISSION_GRANTED
                if (notGiant) {
                    return false
                }
            }
        }
        return true
    }

    @JvmStatic
    fun hasShowRequestPermissionRationale(
            context: Context?,
            vararg permissions: String?
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
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

    @JvmStatic
    fun allPermissionGrant(intArray: IntArray): Boolean {
        var isGranted = true
        for (permission in intArray) {
            if (permission != PackageManager.PERMISSION_GRANTED) {
                isGranted = false
                break
            }
        }
        return isGranted
    }

    @JvmStatic
    fun getBackgroundColor(context: Context): Int {
        val a = TypedValue()
        context.theme.resolveAttribute(android.R.attr.windowBackground, a, true)
        return a.data
    }

    @JvmStatic
    fun fetchAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
                context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    @JvmStatic
    fun fetchTextColor(context: Context): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
                context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorOnBackground))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

    @JvmStatic
    fun fetchSecondaryTextColor(context: Context): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
                context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorSecondary))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }


    @JvmStatic
    fun isTablet(resources: Resources): Boolean {
        return resources.configuration.smallestScreenWidthDp >= 600
    }

    @JvmStatic
    fun isLandscape(resources: Resources): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    @JvmStatic
    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    @JvmStatic
    fun isEqualAndroidQ(): Boolean {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
    }

    @JvmStatic
    fun isAndroidM(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    @JvmStatic
    fun isAndroidO(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    @JvmStatic
    fun isAndroidQ(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    @JvmStatic
    fun isAndroidR(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    @JvmStatic
    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    @JvmStatic
    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    @JvmStatic
    fun convertPixelsToDp(px: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    @JvmStatic
    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                    1,
                    1,
                    Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @JvmStatic
    fun getStoragePermissions(): Array<String> {
        return if (isAndroidQ()) {
            Constants.STORAGE_PERMISSION_STORAGE_SCOPE
        } else {
            Constants.STORAGE_PERMISSION_UNDER_STORAGE_SCOPE
        }
    }

    fun showAlertPermissionNotGrant(binding:ViewDataBinding, activity: Activity) {
        if (!hasShowRequestPermissionRationale(activity, *getStoragePermissions())) {
            val snackBar = Snackbar.make(
                binding.root,
                activity.resources.getString(R.string.goto_settings),
                Snackbar.LENGTH_LONG
            )
            snackBar.setAction(
                activity.resources.getString(R.string.settings)
            ) { view: View? ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            }
            snackBar.show()
        } else {
            Toast.makeText(
                activity,
                activity.resources.getString(R.string.grant_permission),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @JvmStatic
    fun screenShot(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(
                view.width,
                view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun Bitmap.toRoundedCorners(
            topLeftRadius: Float = 0F,
            topRightRadius: Float = 0F,
            bottomRightRadius: Float = 0F,
            bottomLeftRadius: Float = 0F
    ):Bitmap{
        val bitmap = Bitmap.createBitmap(
                width, // width in pixels
                height, // height in pixels
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        // the bounds of a round-rectangle to add to the path
        val rectF = RectF(0f,0f,width.toFloat(),height.toFloat())

        // float array of 8 values, 4 pairs of [x,y] radii
        val radii = floatArrayOf(
                topLeftRadius,topLeftRadius, // top left corner
                topRightRadius,topRightRadius, // top right corner
                bottomRightRadius,bottomRightRadius, // bottom right corner
                bottomLeftRadius,bottomLeftRadius // bottom left corner
        )

        // path to draw rounded corners bitmap
        val path = Path().apply {
            // add a closed round-rectangle contour to the path
            // each corner receives two radius values [x, y]
            addRoundRect(
                    rectF,
                    radii,
                    // the direction to wind the round-rectangle's contour
                    Path.Direction.CCW
            )
        }

        // intersect the current clip with the specified path
        canvas.clipPath(path)

        // draw the rounded corners bitmap on canvas
        canvas.drawBitmap(this,0f,0f,null)
        return bitmap
    }


    @JvmStatic
    fun getCaptureMatrix(data: ByteArray, frontCamera: Boolean): Matrix {
        val inputStream: InputStream = ByteArrayInputStream(data)
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val orientation = ei!!.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        val rotateDegree: Int = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        val matrix = Matrix()
        matrix.postRotate((rotateDegree).toFloat())
        if(frontCamera){
            matrix.postScale(-1f, 1f)
        }
        return matrix
    }

    fun shareMedia(context: Context, uris:MutableList<Uri>){
        val sharingIntent = if(uris.size ==1){
            Intent().apply {
                action= Intent.ACTION_SEND
                type= "image/*"
                putExtra(Intent.EXTRA_STREAM, uris[0])
            }
        }else{
            Intent().apply {
                action= Intent.ACTION_SEND_MULTIPLE
                type= "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris as ArrayList<out Parcelable>)
            }
        }
        context.startActivity(Intent.createChooser(sharingIntent, "Share Image"))
    }

    fun shareApp(context: Context, appPackage:String){
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackage")))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackage")))
        }
    }

//    fun sendEmailRateApp(context: Context, rateNumber:Int, feedBack: String?){
//        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("contact@hola360.com"))
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.rate_app_email_subject))
//        var content= String.format("%s\n%s", context.resources.getString(R.string.rate_app_email_content_1),
//            String.format("%s %s", context.resources.getString(R.string.rate_app_email_content_2), rateNumber))
//        if(!feedBack.isNullOrEmpty()){
//            content+= "\n"
//            content+= "- ".plus(feedBack)
//        }
//        emailIntent.putExtra(Intent.EXTRA_TEXT, content)
//        context.startActivity(emailIntent)
//    }

    fun getInternalStorageInformation(file:File):String{
        val iStat= StatFs(file.path)
        val iBlockSize= iStat.blockSizeLong
        val availableBlock= iStat.availableBlocksLong
        val totalBlock= iStat.blockCountLong
        val availableSpace= formatSize(availableBlock*iBlockSize)
        val totalSpace= formatSize(totalBlock*iBlockSize)
        return String.format("$availableSpace free of $totalSpace")
    }

    fun getInternalStoragePercent(file:File):Float{
        val iStat= StatFs(file.path)
        val availableBlock= iStat.availableBlocksLong
        val totalBlock= iStat.blockCountLong
        return availableBlock.toFloat()/totalBlock
    }

    fun formatSize(storageSize: Long): String? {
        var size = storageSize
        var suffix: String? = null
        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024
                if (size >= 1024) {
                    suffix = "GB"
                    size /= 1024
                }
            }
        }
        val resultBuffer = StringBuilder(size.toString())
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }

    fun getScreenWidth(activity: Activity):Int{
        val displayMetrics = DisplayMetrics()
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getScreenHeight(activity: Activity):Int{
        val displayMetrics = DisplayMetrics()
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getBatteryPercent(context: Context):Int{
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val batteryPct = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
        return batteryPct?.toInt() ?: 100
    }

    interface OnStorageRequestResult{
        fun onGranted()
        fun onDeny()
    }

}