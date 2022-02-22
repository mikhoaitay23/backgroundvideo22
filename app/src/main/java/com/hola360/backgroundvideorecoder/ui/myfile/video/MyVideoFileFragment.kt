package com.hola360.backgroundvideorecoder.ui.myfile.video

import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.databinding.LayoutMyVideoFileBinding
import com.hola360.backgroundvideorecoder.ui.myfile.adapter.MyFileSessionAdapter
import com.hola360.backgroundvideorecoder.ui.myfile.audio.MyAudioFileViewModel
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.utils.Utils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

class MyVideoFileFragment : BaseRecordPageFragment<LayoutMyVideoFileBinding>(),
    MyFileSessionAdapter.OnClickListener {

    private lateinit var viewModel: MyVideoFileViewModel
    private var mFileSectionAdapter = SectionedRecyclerViewAdapter()

    override val layoutId = R.layout.layout_my_video_file

    override fun initView() {


        binding!!.lifecycleOwner = this
        binding!!.viewModel = viewModel
    }

    override fun initViewModel() {
        val factory = MyVideoFileViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[MyVideoFileViewModel::class.java]

        viewModel.fetch()

        viewModel.allFileLiveData.observe(this) {
            it?.let {
                when (it.loadDataStatus) {
                    LoadDataStatus.LOADING -> {
                        binding!!.mProgressBar.visibility = View.VISIBLE
                    }
                    LoadDataStatus.SUCCESS -> {
                        val body = (it as DataResponse.DataSuccessResponse).body
                        val groupedHashMap = Utils.groupDataIntoHashMap(requireContext(), body)
                        mFileSectionAdapter.removeAllSections()
                        for ((key, value) in groupedHashMap?.entries!!) {
                            if (value != null) {
                                if (value.size > 0) {
                                    mFileSectionAdapter.addSection(
                                        MyFileSessionAdapter(
                                            key,
                                            value.toMutableList(),
                                            this
                                        )
                                    )
                                }
                            }
                        }
                        binding!!.rcMyVideoFile.adapter = mFileSectionAdapter
                        mFileSectionAdapter.notifyDataSetChanged()
                        binding!!.mProgressBar.visibility = View.GONE
                    }
                    LoadDataStatus.ERROR -> {
                        binding!!.mProgressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onOptionClicked(position: Int, view: View) {
        showActionItem(view)
    }

    private fun showActionItem(view: View) {
        val popupMenu = PopupMenu(
            ContextThemeWrapper(
                context,
                R.style.PopupMenuTheme
            ), view.findViewById(R.id.btnOption)
        )
        with(popupMenu) {
            inflate(R.menu.menu_option_my_file)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_select -> {

                    }
                    R.id.action_open_with -> {

                    }
                    R.id.action_rename -> {

                    }
                    R.id.action_delete -> {

                    }
                    R.id.action_share -> {

                    }
                }
                false
            }
            show()
        }
    }
}