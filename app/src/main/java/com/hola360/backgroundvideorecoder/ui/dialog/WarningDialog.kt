package com.hola360.backgroundvideorecoder.ui.dialog

import android.widget.LinearLayout
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutBatteryWarningBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseDialog

class WarningDialog(val callback:ConfirmDialog.OnConfirmOke,
    private val dismissCallback:OnDialogDismiss): BaseDialog<LayoutBatteryWarningBinding>() {

    private var isBattery= false

    override val layoutId: Int
        get() = R.layout.layout_battery_warning

    override fun initView() {
        binding!!.isBattery= isBattery
        binding!!.cancel.setOnClickListener { dismiss() }
        binding!!.oke.setOnClickListener {
            callback.onConfirm()
            dismiss()
        }
    }

    fun setBatteryOrStorageType(isBattery: Boolean){
        this.isBattery= isBattery
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissCallback.onDismiss()
    }
}