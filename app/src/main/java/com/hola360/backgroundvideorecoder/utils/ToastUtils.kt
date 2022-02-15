package com.hola360.backgroundvideorecoder.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast

class ToastUtils(context: Context?) {
    fun showToast(message: String?) {
        toast!!.setText(message)
        toast!!.duration = Toast.LENGTH_SHORT
        toast!!.show()
    }

    companion object {
        private var toast: Toast? = null
        private var instance: ToastUtils? = null

        fun getInstance(context: Context?): ToastUtils? {
            if (instance == null) {
                instance = ToastUtils(context)
            }
            return instance
        }
    }

    init {
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT)
        toast!!.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 70)
    }

    fun release() {
        instance = null
    }
}