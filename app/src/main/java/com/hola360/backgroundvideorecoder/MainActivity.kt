package com.hola360.backgroundvideorecoder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.hola360.backgroundvideorecoder.databinding.ActivityMainBinding
import com.hola360.backgroundvideorecoder.service.RecordService
import com.hola360.backgroundvideorecoder.utils.DataSharePreferenceUtil
import com.hola360.backgroundvideorecoder.widget.Toolbar
import android.content.ComponentName
import android.content.Intent

import android.os.IBinder

import android.content.ServiceConnection
import android.util.Log


class MainActivity : AppCompatActivity(), RecordService.Listener {

    private var binding: ActivityMainBinding? = null
    private var navController: NavController? = null
    private var navHostFragment: Fragment? = null
    private var recordService: RecordService? = null
    var intentService: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupNavigation()
        setupToolbar()
        setupPrivacy()
        bindActivityToService()
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

    fun bindActivityToService(){
        val intent= Intent(this, RecordService::class.java)
        bindService(intent, mConnection, BIND_AUTO_CREATE)
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

    fun startRecordVideo(status:Int){
        val intent= Intent(this, RecordService::class.java)
        intent.putExtra("Video_status", status)
        startService(intent)
    }

    val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            Toast.makeText(this@MainActivity, "onServiceConnected", Toast.LENGTH_SHORT)
                .show()
            val binder: RecordService.LocalBinder = service as RecordService.LocalBinder
            recordService = binder.getServiceInstance()
            recordService!!.registerListener(this@MainActivity)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Toast.makeText(this@MainActivity, "onServiceDisconnected", Toast.LENGTH_SHORT)
                .show()
        }
    }

    companion object {
        const val PRIVACY = "privacy"
    }

    override fun isStarted() {

    }
}