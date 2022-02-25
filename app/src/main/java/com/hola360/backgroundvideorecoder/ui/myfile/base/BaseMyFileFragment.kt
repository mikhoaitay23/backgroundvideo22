package com.hola360.backgroundvideorecoder.ui.myfile.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment
import com.hola360.backgroundvideorecoder.ui.myfile.MyFileViewModel
import com.hola360.backgroundvideorecoder.ui.myfile.adapter.MyFileSectionAdapter
import com.hola360.backgroundvideorecoder.ui.myfile.video.MyFileVideo
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.utils.Configurations
import com.hola360.backgroundvideorecoder.utils.Utils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

abstract class BaseMyFileFragment<V: ViewDataBinding>:BaseRecordPageFragment<V>() {

    protected var mSectionRecyclerViewAdapter = SectionedRecyclerViewAdapter()
    protected var mFileSectionAdapter: MyFileSectionAdapter? = null
    protected val myFileViewModel:MyFileViewModel by lazy {
        ViewModelProvider(this, MyFileViewModel.Factory(requireActivity().application))[MyFileViewModel::class.java]
    }
    protected val onAdapterItemClick= object :MyFileSectionAdapter.OnClickListener{
        override fun onSectionHeaderClick() {

        }

        override fun onItemClicked(position: Int, view: View) {

        }
    }

    override fun initViewModel() {

    }

    abstract fun onLoadingData()

    abstract fun onLoadDataSuccess()

    abstract fun onLoadDataError()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val curPath= if(this is MyFileVideo){
            dataPref!!.getParentPath().plus("/")
                .plus(Configurations.RECORD_PATH).plus("/")
                .plus(Configurations.RECORD_VIDEO_PATH)
        }else{
            dataPref!!.getParentPath().plus("/")
                .plus(Configurations.RECORD_PATH).plus("/")
                .plus(Configurations.RECORD_AUDIO_PATH)
        }
        myFileViewModel.fetch(curPath)
    }

    fun onSelectOption(){

    }

    abstract fun onSelectAllOption()

    abstract fun onSortOption()



}