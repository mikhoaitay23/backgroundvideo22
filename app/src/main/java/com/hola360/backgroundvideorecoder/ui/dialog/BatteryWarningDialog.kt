package com.hola360.backgroundvideorecoder.ui.dialog

import android.widget.LinearLayout
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutBatteryWarningBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseDialog

class BatteryWarningDialog(val callback:OnBatteryLowAccept,
                           private val dismissCallback:OnDialogDismiss): BaseDialog<LayoutBatteryWarningBinding>() {

    override val layoutId: Int
        get() = R.layout.layout_battery_warning

    override fun initView() {
        binding!!.cancel.setOnClickListener { dismiss() }
        binding!!.oke.setOnClickListener {
            callback.onBatteryLowAccept()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissCallback.onDismiss()
    }

    interface OnBatteryLowAccept{
        fun onBatteryLowAccept()
    }
}