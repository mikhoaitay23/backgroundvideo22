package com.hola360.backgroundvideorecoder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioFormat
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.hola360.backgroundvideorecoder.data.model.audio.AudioMode
import com.hola360.backgroundvideorecoder.data.model.audio.AudioModel
import com.hola360.backgroundvideorecoder.data.model.audio.AudioQuality
import com.hola360.backgroundvideorecoder.databinding.ActivityMainBinding
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.AudioRecordUtils
import com.hola360.backgroundvideorecoder.ui.record.audio.utils.RecordManager
import com.hola360.backgroundvideorecoder.ui.record.video.VideoRecordFragment
import com.hola360.backgroundvideorecoder.utils.DataSharePreferenceUtil
import com.hola360.backgroundvideorecoder.utils.VideoRecordUtils
import com.hola360.backgroundvideorecoder.widget.Toolbar
import com.zlw.main.recorderlib.recorder.RecordConfig
import java.util.*


class MainActivity : AppCompatActivity(), RecordService.Listener {

    private var binding: ActivityMainBinding? = null
    private var navController: NavController? = null
    private var navHostFragment: Fragment? = null
    var recordService: RecordService? = null
    private var bound = false
    var recordStatus: Int = NO_RECORDING
    private var dataSharedPreferenceUtil: DataSharePreferenceUtil? = null
    var audioModel: AudioModel? = null
    private var recordManager = RecordManager()
    private var audioRecordUtils = AudioRecordUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupNavigation()
        setupToolbar()
        setupPrivacy()
        bindService()

        dataSharedPreferenceUtil = DataSharePreferenceUtil.getInstance(this)
        recordManager.init(application, BuildConfig.DEBUG)
    }

    override fun onStop() {
        super.onStop()
        unbindService()
    }

    override fun onResume() {
        super.onResume()
        audioModel = if (!dataSharedPreferenceUtil!!.getAudioConfig().isNullOrEmpty()) {
            Gson().fromJson(dataSharedPreferenceUtil!!.getAudioConfig(), AudioModel::class.java)
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
    }

    private fun setupPrivacy() {
        val dataPref = DataSharePreferenceUtil.getInstance(this)
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

    fun startRecordVideo(status: Int) {
        recordStatus = status
        if (recordService != null) {
            handleRecordStatus(status)
        } else {
            bindService()
        }
    }

    fun startRecordAudio(status: Int) {
        if (recordService != null) {
            val intent = Intent(this, RecordService::class.java)
            intent.putExtra("Audio", 0)
            startService(intent)
        } else {
            bindService()
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder: RecordService.LocalBinder = service as RecordService.LocalBinder
            recordService = binder.getServiceInstance()
            recordService!!.registerListener(this@MainActivity)
            bound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    private fun handleRecordStatus(status: Int) {
        when (status) {
            RECORD_VIDEO, STOP_VIDEO_RECORD,
            SCHEDULE_RECORD_VIDEO, CANCEL_SCHEDULE_RECORD_VIDEO -> {
                recordService!!.recordVideo(status)
            }
            AUDIO_RECORD -> {
                initRecord()
            }
            STOP_AUDIO_RECORD -> {

            }
        }
    }

    private fun initRecord() {
        val recordDir = String.format(
            Locale.getDefault(), "%s/Record/backgroundrecord/",
            Environment.getExternalStorageDirectory().absolutePath
        )
        recordManager.changeRecordDir(recordDir)
        recordManager.changeFormat(RecordConfig.RecordFormat.MP3)
        recordManager.changeRecordConfig(
            RecordConfig(
                RecordConfig.RecordFormat.MP3,
                AudioMode.obtainMode(audioModel!!.mode),
                AudioFormat.ENCODING_PCM_16BIT,
                AudioQuality.obtainQuality(audioModel!!.quality).toInt()
            )
        )
        recordManager.start()
    }

    fun bindService() {
        Intent(this@MainActivity, RecordService::class.java).also {
            bindService(it, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        if (bound) {
//            recordService!!.registerListener(null)
            unbindService(mConnection)
            bound = false
        }
    }

    override fun onRecordStarted(status: Int) {
        this.recordStatus = status
    }

    override fun updateRecordTime(time: Long, status: Int) {
        if (recordStatus != status) {
            recordStatus = status
        }
        if (navHostFragment?.isAdded == true) {
            val curFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
            curFragment?.let {
                if (it is VideoRecordFragment && recordStatus == RECORD_VIDEO) {
                    it.updateRecodingTime(time)
                }
            }
        }
    }

    override fun onRecordCompleted() {
        this.recordStatus = NO_RECORDING
        if (navHostFragment?.isAdded == true) {
            val curFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
            curFragment?.let {
                if (it is VideoRecordFragment) {
                    it.onRecordCompleted()
                }
            }
        }
    }

    companion object {
        const val PRIVACY = "privacy"
        const val NO_RECORDING = 0
        const val RECORD_VIDEO = 1
        const val STOP_VIDEO_RECORD = 2
        const val SCHEDULE_RECORD_VIDEO = 3
        const val CANCEL_SCHEDULE_RECORD_VIDEO = 4
        const val AUDIO_RECORD = 10
        const val STOP_AUDIO_RECORD = 11
    }
}