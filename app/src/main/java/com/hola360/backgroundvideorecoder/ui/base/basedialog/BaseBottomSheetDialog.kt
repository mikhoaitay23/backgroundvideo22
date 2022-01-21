package com.hola360.backgroundvideorecoder.ui.base.basedialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hola360.backgroundvideorecoder.R

abstract class BaseBottomSheetDialog<V: ViewDataBinding?>: BottomSheetDialogFragment() {

    protected var binding: V?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        initView()
        return binding!!.root
    }

    abstract fun initView()
    abstract fun getLayout(): Int

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null) {
            binding!!.unbind()
        }
    }

}