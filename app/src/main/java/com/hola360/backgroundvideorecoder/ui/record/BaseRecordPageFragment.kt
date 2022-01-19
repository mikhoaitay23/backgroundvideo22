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
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.utils.DataSharePreferenceUtil

abstract class BaseRecordPageFragment<V : ViewDataBinding?> : Fragment() {
    private var dataPref: DataSharePreferenceUtil?= null
    @JvmField
    protected var binding: V? = null
    protected abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataPref= DataSharePreferenceUtil.getInstance(requireActivity())
        initViewModel()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        initView()
        return binding!!.root
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