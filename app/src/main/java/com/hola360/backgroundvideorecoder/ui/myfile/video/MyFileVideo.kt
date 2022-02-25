package com.hola360.backgroundvideorecoder.ui.myfile.video

import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.databinding.LayoutMyVideoFileBinding
import com.hola360.backgroundvideorecoder.ui.myfile.adapter.MyFileSectionAdapter
import com.hola360.backgroundvideorecoder.ui.myfile.base.BaseMyFileFragment
import com.hola360.backgroundvideorecoder.utils.Utils

class MyFileVideo: BaseMyFileFragment<LayoutMyVideoFileBinding>() {

    override val layoutId: Int = R.layout.layout_my_video_file

    override fun initViewModel() {
        myFileViewModel.allFileLiveData.observe(this){
            when(it.loadDataStatus){
                LoadDataStatus.LOADING->{
                    onLoadingData()
                }
                LoadDataStatus.SUCCESS->{
                    onLoadDataSuccess()
                }
                else->{
                    onLoadDataError()
                }
            }
        }
    }

    override fun onLoadingData() {
        binding!!.mProgressBar.visibility = View.VISIBLE
    }

    override fun onLoadDataSuccess() {
        val groupedHashMap = Utils.groupDataIntoHashMap(requireContext(), myFileViewModel.filterList)
        mSectionRecyclerViewAdapter.removeAllSections()
        for ((key, value) in groupedHashMap?.entries!!) {
            if (value != null) {
                if (value.size > 0) {
                    mFileSectionAdapter = MyFileSectionAdapter(
                        key, value.toMutableList(), onAdapterItemClick)
                    mSectionRecyclerViewAdapter.addSection(mFileSectionAdapter)
                }
            }
        }
        mSectionRecyclerViewAdapter.notifyDataSetChanged()
        binding!!.rcMyVideoFile.adapter = mSectionRecyclerViewAdapter
        binding!!.mProgressBar.visibility = View.GONE
    }

    override fun onLoadDataError() {
        binding!!.mProgressBar.visibility = View.GONE
    }

    override fun initView() {

    }

    override fun onSelectAllOption() {

    }

    override fun onSortOption() {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }
}