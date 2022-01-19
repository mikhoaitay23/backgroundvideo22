package com.hola360.backgroundvideorecoder.ui.base.basefragment

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

abstract class BaseFragment<V : ViewDataBinding?> : Fragment() {
    protected var dataPref: DataSharePreferenceUtil?= null
    @JvmField
    var binding: V? = null
    protected abstract val layoutId: Int
    protected abstract val showToolbar:Boolean
    protected abstract val toolbarTitle: String?
    protected abstract val menuCode: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataPref= DataSharePreferenceUtil.getInstance(requireActivity())
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

    override fun onResume() {
        super.onResume()
        if(showToolbar){
            (requireActivity() as MainActivity).showToolbar()
        }else{
            (requireActivity() as MainActivity).hideToolbar()
        }
        (requireActivity() as MainActivity).showToolbarMenu(menuCode)
        (requireActivity() as MainActivity).setToolbarTitle(toolbarTitle)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding != null) {
            binding!!.unbind()
        }
    }
}