package com.hola360.backgroundvideorecoder.ui.dialog

import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentBottomSheetConfirmBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog

class ConfirmDialog(val callback:OnConfirmOke,
                    private val dismissCallback:OnDialogDismiss):BaseBottomSheetDialog<FragmentBottomSheetConfirmBinding>() {

    private var messsages:String?= null

    override fun getLayout(): Int = R.layout.fragment_bottom_sheet_confirm

    override fun initView() {
        binding!!.btnNegative.setOnClickListener {
            dismiss()
        }
        binding!!.btnPositive.setOnClickListener {
            callback.onConfirm()
            dismiss()
        }
    }

    fun setMessages(messages:String){
        this.messsages= messages
    }

    override fun onResume() {
        super.onResume()
        binding!!.tvMsg.text= messsages
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissCallback.onDismiss()
    }

    interface OnConfirmOke{
        fun onConfirm()
    }
}