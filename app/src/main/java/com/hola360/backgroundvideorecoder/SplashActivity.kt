package com.hola360.backgroundvideorecoder

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        intentToMain()
    }

    private fun intentToMain(){
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }

    companion object{
        const val SPLASH_DELAY= 2000L
    }
}