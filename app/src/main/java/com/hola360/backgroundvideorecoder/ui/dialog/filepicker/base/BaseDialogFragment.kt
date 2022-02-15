package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.base

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.hola360.backgroundvideorecoder.MainActivity


abstract class BaseDialogFragment<V : ViewDataBinding> : DialogFragment() {
    protected var binding: V? = null
    protected lateinit var mainActivity: MainActivity
    private var builder: AlertDialog.Builder? = null
    protected var mDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = (requireActivity() as MainActivity)
        initViewModel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding =
            DataBindingUtil.inflate(layoutInflater, getLayout(), null, false)
        builder = AlertDialog.Builder(mainActivity)
        builder!!.setView(binding!!.root)
        mDialog = builder!!.create()
        binding!!.lifecycleOwner = this
        initView()
        return mDialog!!
    }

    abstract fun getLayout(): Int
    abstract fun initView()
    abstract fun initViewModel()
}