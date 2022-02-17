package com.hola360.backgroundvideorecoder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.databinding.ActivityMainBinding
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import com.hola360.backgroundvideorecoder.utils.SystemUtils
import com.hola360.backgroundvideorecoder.utils.ToastUtils
import com.hola360.backgroundvideorecoder.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var navController: NavController? = null
    private var navHostFragment: Fragment? = null
    var recordService: RecordService? = null
    var isBound = false
    private var dataSharedPreferenceUtils: SharedPreferenceUtils? = null
    var audioModel: AudioModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        dataSharedPreferenceUtils = SharedPreferenceUtils.getInstance(this)
        setupNavigation()
        setupToolbar()
        setupPrivacy()
        bindService()
        setParentPath()
    }

    private fun setParentPath() {
        val path = dataSharedPreferenceUtils!!.getParentPath()
        if (path == null || path == "") {
            dataSharedPreferenceUtils!!.setParentPath(this.externalCacheDir!!.absolutePath)
        }
    }

    override fun onResume() {
        super.onResume()
        audioModel = if (!dataSharedPreferenceUtils!!.getAudioConfig().isNullOrEmpty()) {
            Gson().fromJson(dataSharedPreferenceUtils!!.getAudioConfig(), AudioModel::class.java)
        } else {
            AudioModel()
        }
    }

    private fun setupNavigation() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_main)
        navController = findNavController(R.id.nav_host_fragment_main)
    }

    private fun setupToolbar() {
        binding!!.toolbar.setupToolbarCallback(object : Toolbar.CustomToolbarCallback {
            override fun onBack() {
                navController?.popBackStack()
            }
        })
        SCREEN_WIDTH = SystemUtils.getScreenWidth(this)
        SCREEN_HEIGHT = SystemUtils.getScreenHeight(this)
    }

    private fun setupPrivacy() {
        val dataPref = SharedPreferenceUtils.getInstance(this)
        if (!dataPref!!.getBooleanValue(PRIVACY)) {
            navController!!.navigate(R.id.nav_confirm_privacy)
        }
    }

    fun hideToolbar() {
        binding?.showToolbar = false
    }

    fun showToolbar() {
        binding?.showToolbar = true
    }

    fun setToolbarTitle(title: String?) {
        binding?.toolbar?.setToolbarTitle(title)
    }

    fun showToolbarMenu(menuCode: Int) {
        binding?.toolbar?.showToolbarMenu(menuCode)
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder: RecordService.LocalBinder = service as RecordService.LocalBinder
            recordService = binder.getServiceInstance()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    private fun bindService() {
        val intent = Intent(this, RecordService::class.java)
        startService(intent)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    fun showToast(message: String) {
        ToastUtils.getInstance(this)!!.showToast(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        ToastUtils.getInstance(this)!!.release()
        if (isBound) {
            unbindService(mConnection)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        SCREEN_ORIENTATION = newConfig.orientation
    }

    companion object {
        var SCREEN_WIDTH: Int = 0
        var SCREEN_HEIGHT: Int = 0
        var SCREEN_ORIENTATION: Int = 1
        const val PRIVACY = "privacy"
        const val NO_RECORDING = 0
        const val RECORD_VIDEO = 1
        const val STOP_VIDEO_RECORD = 2
        const val SCHEDULE_RECORD_VIDEO = 3
        const val CANCEL_SCHEDULE_RECORD_VIDEO = 4
        const val RECORD_VIDEO_LOW_BATTERY = 5
    }
}