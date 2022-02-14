package com.hola360.backgroundvideorecoder.ui.dialog

import android.widget.LinearLayout
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutBatteryWarningBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseDialog

class BatteryWarningDialog: BaseDialog<LayoutBatteryWarningBinding>() {

    override val layoutId: Int
        get() = R.layout.layout_battery_warning

    override fun initView() {

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }
}