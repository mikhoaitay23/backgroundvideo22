package com.hola360.backgroundvideorecoder.ui.record

import android.os.Build
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils

abstract class BaseRecordPageFragment<V : ViewDataBinding?> : Fragment() {
    protected var dataPref: SharedPreferenceUtils?= null
    @JvmField
    protected var binding: V? = null
    protected abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataPref= SharedPreferenceUtils.getInstance(requireActivity())
        initViewModel()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    protected abstract fun initView()
    protected abstract fun initViewModel()


    override fun onDestroy() {
        super.onDestroy()
        if (binding != null) {
            binding!!.unbind()
        }
    }
}