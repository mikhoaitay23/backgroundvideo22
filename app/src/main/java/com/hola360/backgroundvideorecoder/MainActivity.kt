package com.hola360.backgroundvideorecoder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.hola360.backgroundvideorecoder.databinding.ActivityMainBinding
import com.hola360.backgroundvideorecoder.utils.DataSharePreferenceUtil
import com.hola360.backgroundvideorecoder.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding?= null
    private var navController: NavController?= null
    private var navHostFragment: Fragment?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupNavigation()
        setupToolbar()
        setupPrivacy()
    }

    private fun setupNavigation(){
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_main)
        navController= findNavController(R.id.nav_host_fragment_main)
    }

    private fun setupToolbar(){
        binding!!.toolbar.setupToolbarCallback(object : Toolbar.CustomToolbarCallback{
            override fun onBack() {
                navController?.popBackStack()
            }
        })
    }

    private fun setupPrivacy(){
        val dataPref= DataSharePreferenceUtil.getInstance(this)
        if(!dataPref!!.getBooleanValue(PRIVACY)){
            navController!!.navigate(R.id.nav_confirm_privacy)
        }
    }

    fun hideToolbar(){
        binding?.showToolbar=false
    }

    fun showToolbar(){
        binding?.showToolbar=true
    }

    fun setToolbarTitle(title:String?){
        binding?.toolbar?.setToolbarTitle(title)
    }

    fun showToolbarMenu(menuCode:Int){
        binding?.toolbar?.showToolbarMenu(menuCode)
    }

    companion object{
        const val  PRIVACY= "privacy"
    }
}