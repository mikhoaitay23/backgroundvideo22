package com.hola360.backgroundvideorecoder.ui.dialog.input

import android.os.Bundle
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutDialogInputTextBinding
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.base.BaseDialogFragment
import com.hola360.backgroundvideorecoder.utils.Constants
import java.util.regex.Pattern

class InputTextDialog : BaseDialogFragment<LayoutDialogInputTextBinding>() {
    var onInputDialogResponse: OnInputDialogResponse? = null
    override fun getLayout(): Int {
        return R.layout.layout_dialog_input_text
    }

    override fun initView() {
        val oldName = requireArguments().getString(TITLE)
        binding!!.myEditTextName.setText(oldName)
        binding!!.myButtonCancel.setOnClickListener {
            onInputDialogResponse?.onDismiss()
            dismiss()
        }
        binding!!.myButtonOk.setOnClickListener {
            if (!Pattern.compile(Constants.REGEX_FILENAME)
                    .matcher(binding!!.myEditTextName.editableText.toString().trim()).matches()
            ) {
                mainActivity.showToast(getString(R.string.invalid_filename))
                return@setOnClickListener
            } else {
                onInputDialogResponse?.onConfirm(
                    binding!!.myEditTextName.editableText.toString().trim()
                )
                dismiss()
            }
        }
    }

    interface OnInputDialogResponse {
        fun onDismiss()
        fun onConfirm(value: String?)
    }

    companion object {
        private const val TITLE = "title"
        fun create(oldName: String): InputTextDialog {
            val dialogFragment = InputTextDialog()
            val bundle = Bundle()
            bundle.putString(TITLE, oldName)
            dialogFragment.arguments = bundle
            return dialogFragment
        }
    }

    override fun initViewModel() {
    }

}