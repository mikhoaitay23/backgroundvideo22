package com.hola360.backgroundvideorecoder.ui.myfile.audio

import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.databinding.LayoutMyAudioFileBinding
import com.hola360.backgroundvideorecoder.ui.myfile.adapter.MyFileSectionAdapter
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.utils.Utils.groupDataIntoHashMap
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

class MyAudioFileFragment : BaseRecordPageFragment<LayoutMyAudioFileBinding>(),
    MyFileSectionAdapter.OnClickListener {

    private lateinit var viewModel: MyAudioFileViewModel
    private var mFileSectionAdapter = SectionedRecyclerViewAdapter()

    override val layoutId = R.layout.layout_my_audio_file

    override fun initView() {
        binding!!.lifecycleOwner = this
        binding!!.viewModel = viewModel
    }

    override fun initViewModel() {
        val factory = MyAudioFileViewModel.Factory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory)[MyAudioFileViewModel::class.java]

        viewModel.fetch()

        viewModel.allFileLiveData.observe(this) {
            it?.let {
                when (it.loadDataStatus) {
                    LoadDataStatus.LOADING -> {
                        binding!!.mProgressBar.visibility = View.VISIBLE
                    }
                    LoadDataStatus.SUCCESS -> {
                        val body = (it as DataResponse.DataSuccessResponse).body
                        val groupedHashMap = groupDataIntoHashMap(requireContext(), body)
                        mFileSectionAdapter.removeAllSections()
                        for ((key, value) in groupedHashMap?.entries!!) {
                            if (value != null) {
                                if (value.size > 0) {
                                    mFileSectionAdapter.addSection(
                                        MyFileSectionAdapter(
                                            key,
                                            value.toMutableList(),
                                            this
                                        )
                                    )
                                }
                            }
                        }
                        binding!!.rcMyAudioFile.adapter = mFileSectionAdapter
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

    override fun onItemClicked(position: Int, view: View) {
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
            inflate(R.menu.menu_option_item_my_file)
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

    override fun onSectionHeaderClick() {

    }
}