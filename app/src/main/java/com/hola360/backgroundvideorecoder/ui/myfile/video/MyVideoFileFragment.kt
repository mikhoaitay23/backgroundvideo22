package com.hola360.backgroundvideorecoder.ui.myfile.video

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.model.mediafile.MediaFile
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.databinding.LayoutMyVideoFileBinding
import com.hola360.backgroundvideorecoder.ui.dialog.ConfirmDialog
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.input.InputTextDialog
import com.hola360.backgroundvideorecoder.ui.myfile.adapter.MyFileSectionAdapter
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.utils.Utils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

class MyVideoFileFragment : BaseRecordPageFragment<LayoutMyVideoFileBinding>(),
    MyFileSectionAdapter.OnClickListener, View.OnClickListener {

    private lateinit var viewModel: MyVideoFileViewModel
    private var mSectionRecyclerViewAdapter = SectionedRecyclerViewAdapter()
    private var mFileSectionAdapter: MyFileSectionAdapter? = null
    private var fileList = mutableListOf<MediaFile>()
    private lateinit var mainActivity: MainActivity
    private val confirmCancelSchedule: ConfirmDialog by lazy {
        ConfirmDialog(object : ConfirmDialog.OnConfirmOke {
            override fun onConfirm() {

            }
        }, object : OnDialogDismiss {
            override fun onDismiss() {

            }

        })
    }

    override val layoutId = R.layout.layout_my_video_file

    override fun initView() {
        mainActivity = requireActivity() as MainActivity

        binding!!.btnBack.setOnClickListener(this)
        binding!!.btnOption.setOnClickListener(this)
        binding!!.btnSearch.setOnClickListener(this)
        binding!!.btnSelectAll.setOnClickListener(this)

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
                        mSectionRecyclerViewAdapter.removeAllSections()
                        for ((key, value) in groupedHashMap?.entries!!) {
                            if (value != null) {
                                if (value.size > 0) {
                                    mFileSectionAdapter = MyFileSectionAdapter(
                                        key,
                                        value.toMutableList(),
                                        this
                                    )
                                    mSectionRecyclerViewAdapter.addSection(mFileSectionAdapter)
                                }
                            }
                        }
                        fileList = body
                        binding!!.rcMyVideoFile.adapter = mSectionRecyclerViewAdapter
                        mSectionRecyclerViewAdapter.notifyDataSetChanged()
                        binding!!.mProgressBar.visibility = View.GONE
                    }
                    LoadDataStatus.ERROR -> {
                        binding!!.mProgressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding!!.btnBack -> {
                if (mFileSectionAdapter?.isSelectMode == true) {
                    mFileSectionAdapter?.isSelectMode = false
                    mSectionRecyclerViewAdapter.notifyDataSetChanged()
                    binding!!.isSelectMode = mFileSectionAdapter?.isSelectMode
                } else {
                    findNavController().navigateUp()
                }
            }
            binding!!.btnOption -> {
                showActionItem(binding!!.btnOption)
            }
            binding!!.btnSearch -> {

            }
        }
    }

    override fun onClicked(position: Int, view: View) {
        when (view.id) {
            R.id.btnOption -> {
                showActionItem(position, view)
            }
            R.id.btnSelect -> {
                mFileSectionAdapter?.updateSelected(position)
                binding!!.tvCount.text =
                    mFileSectionAdapter?.countItemSelected().toString().plus(" ")
                        .plus(getString(R.string.selected))
                binding!!.btnSelectAll.isChecked =
                    mFileSectionAdapter?.countItemSelected() == mSectionRecyclerViewAdapter.itemCount - 1
                mSectionRecyclerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showActionItem(position: Int, view: View) {
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
                        if (!mFileSectionAdapter?.isSelectMode!!) {
                            mFileSectionAdapter?.isSelectMode = true
                            mSectionRecyclerViewAdapter.notifyItemRangeChanged(0, fileList.size + 1)
                        }
                        mFileSectionAdapter?.updateSelected(position)
                        binding!!.tvCount.text =
                            mFileSectionAdapter?.countItemSelected().toString().plus(" ")
                                .plus(getString(R.string.selected))
                        mSectionRecyclerViewAdapter.notifyItemChanged(position)
                        binding!!.isSelectMode = mFileSectionAdapter?.isSelectMode
                    }
                    R.id.action_open_with -> {

                    }
                    R.id.action_rename -> {
                        onRename(position)
                    }
                    R.id.action_delete -> {
//                        val messages = resources.getQuantityString(R.plurals.delete_file_msg)
//                        confirmCancelSchedule.setMessages(messages)
//                        confirmCancelSchedule.show(requireActivity().supportFragmentManager, "Confirm")
                    }
                    R.id.action_share -> {

                    }
                }
                false
            }
            show()
        }
    }

    private fun onRename(position: Int) {
        val inputTextDialog = InputTextDialog.create(fileList[position].file.name)
        inputTextDialog.onInputDialogResponse = object : InputTextDialog.OnInputDialogResponse {
            override fun onDismiss() {
            }

            override fun onConfirm(value: String?) {
                if (value!!.isEmpty()) {
                    mainActivity.showToast(getString(R.string.empty_file_name))
                    return
                } else {
                    inputTextDialog.dismissAllowingStateLoss()
                }
            }
        }
        inputTextDialog.show(mainActivity.supportFragmentManager, "editName")
    }

    private fun showActionItem(view: View) {
        val popupMenu = PopupMenu(
            ContextThemeWrapper(
                requireContext(),
                R.style.PopupMenuTheme
            ), view.findViewById(R.id.btnOption)
        )
        with(popupMenu) {
            inflate(R.menu.menu_option_my_file)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_select_all -> {
                        onSelectAll()
                    }
                    R.id.action_sort_by -> {

                    }
                }
                false
            }
            show()
        }
    }

    private fun onSelectAll() {
        binding!!.btnSelectAll.isChecked = !binding!!.btnSelectAll.isChecked
        mFileSectionAdapter?.updateSelect(binding!!.btnSelectAll.isChecked)
        mFileSectionAdapter?.isSelectMode = true
        binding!!.isSelectMode = mFileSectionAdapter?.isSelectMode
        binding!!.tvCount.text =
            mFileSectionAdapter?.countItemSelected().toString().plus(" ")
                .plus(getString(R.string.selected))
        mSectionRecyclerViewAdapter.notifyDataSetChanged()
    }


}