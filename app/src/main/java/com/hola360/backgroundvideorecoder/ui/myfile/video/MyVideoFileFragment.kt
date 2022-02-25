package com.hola360.backgroundvideorecoder.ui.myfile.video

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.LoadDataStatus
import com.hola360.backgroundvideorecoder.data.model.mediafile.MediaFile
import com.hola360.backgroundvideorecoder.data.response.DataResponse
import com.hola360.backgroundvideorecoder.databinding.LayoutMyVideoFileBinding
import com.hola360.backgroundvideorecoder.ui.dialog.ConfirmDialog
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss
import com.hola360.backgroundvideorecoder.ui.dialog.input.InputTextDialog
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionAdapter
import com.hola360.backgroundvideorecoder.ui.dialog.listdialog.ListSelectionBotDialog
import com.hola360.backgroundvideorecoder.ui.myfile.adapter.MyFilePopupAdapter
import com.hola360.backgroundvideorecoder.ui.myfile.adapter.MyFileSectionAdapter
import com.hola360.backgroundvideorecoder.ui.record.BaseRecordPageFragment
import com.hola360.backgroundvideorecoder.utils.SharedPreferenceUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

class MyVideoFileFragment : BaseRecordPageFragment<LayoutMyVideoFileBinding>(),
    MyFileSectionAdapter.OnClickListener, View.OnClickListener {

    private lateinit var viewModel: MyVideoFileViewModel
    private var mSectionRecyclerViewAdapter = SectionedRecyclerViewAdapter()
    private var mFileSectionAdapter: MyFileSectionAdapter? = null
    private var fileList = mutableListOf<MediaFile>()
    private lateinit var mainActivity: MainActivity
    private var mListSelectionBottomSheet: ListSelectionBotDialog? = null
    private var showBottomSheet = false
    private var popupWindow: PopupWindow? = null
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
//        mainActivity = requireActivity() as MainActivity
//
//        binding!!.btnBack.setOnClickListener(this)
//        binding!!.btnOption.setOnClickListener(this)
//        binding!!.btnSearch.setOnClickListener(this)
//        binding!!.btnSelectAll.setOnClickListener(this)
//        binding!!.btnSelectAll.isChecked = false

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
                                        key, value.toMutableList(), this
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
//        when (p0) {
//            binding!!.btnBack -> {
//                if (mFileSectionAdapter?.isSelectMode == true) {
//                    mFileSectionAdapter?.isSelectMode = false
//                    mSectionRecyclerViewAdapter.notifyDataSetChanged()
//                    binding!!.isSelectMode = mFileSectionAdapter?.isSelectMode
//                } else {
//                    findNavController().navigateUp()
//                }
//            }
//            binding!!.btnOption -> {
//                showMenuItem(binding!!.btnOption)
//            }
//            binding!!.btnSearch -> {
//
//            }
//            binding!!.btnSelectAll -> {
//                binding!!.btnSelectAll.isChecked = !binding!!.btnSelectAll.isChecked
//                mFileSectionAdapter?.updateSelect(binding!!.btnSelectAll.isChecked)
//                binding!!.tvCount.text =
//                    mFileSectionAdapter?.countItemSelected().toString().plus(" ")
//                        .plus(getString(R.string.selected))
//                mSectionRecyclerViewAdapter.notifyDataSetChanged()
//            }
//        }
    }

    override fun onItemClicked(position: Int, view: View) {
        when (view.id) {
            R.id.btnOption -> {
                openPopup(view)
            }
            R.id.btnSelect -> {
//                mFileSectionAdapter?.updateSelected(position)
//                binding!!.tvCount.text =
//                    mFileSectionAdapter?.countItemSelected().toString().plus(" ")
//                        .plus(getString(R.string.selected))
//                binding!!.btnSelectAll.isChecked =
//                    mFileSectionAdapter?.countItemSelected() == mSectionRecyclerViewAdapter.itemCount - 1
//                mSectionRecyclerViewAdapter.notifyDataSetChanged()
            }
            R.id.mLayoutRoot -> {
                Utils.openMp4File(requireContext(), fileList[position].file)
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
//                        binding!!.tvCount.text =
//                            mFileSectionAdapter?.countItemSelected().toString().plus(" ")
//                                .plus(getString(R.string.selected))
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

    private fun showMenuItem(view: View) {
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
                        showBottomSheet = true
                        onSortByBottomSheet()
                    }
                }
                false
            }
            show()
        }
    }

    private fun onSelectAll() {
//        mFileSectionAdapter?.updateSelect(binding!!.btnSelectAll.isChecked)
//        mFileSectionAdapter?.isSelectMode = true
//        binding!!.isSelectMode = mFileSectionAdapter?.isSelectMode
//        binding!!.tvCount.text =
//            mFileSectionAdapter?.countItemSelected().toString().plus(" ")
//                .plus(getString(R.string.selected))
//        mSectionRecyclerViewAdapter.notifyDataSetChanged()
    }

    private fun onSortByBottomSheet() {
        val listSelection = resources.getStringArray(R.array.sort_by).toMutableList()
        mListSelectionBottomSheet = ListSelectionBotDialog(
            getString(R.string.sort_by),
            listSelection,
            object : ListSelectionAdapter.OnItemListSelection {
                override fun onSelection(position: Int) {
                    when (position) {
                        0 -> {
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByDate(true)
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByASC(false)
                            startApplyNewSort()
                        }
                        1 -> {
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByDate(true)
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByASC(true)
                            startApplyNewSort()
                        }
                        2 -> {
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortBySize(true)
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByASC(false)
                            startApplyNewSort()
                        }
                        3 -> {
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortBySize(true)
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByASC(true)
                            startApplyNewSort()
                        }
                        4 -> {
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByName(true)
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByASC(false)
                            startApplyNewSort()
                        }
                        5 -> {
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByName(true)
                            SharedPreferenceUtils.getInstance(mainActivity)?.setSortByASC(true)
                            startApplyNewSort()
                        }
                    }
                    mListSelectionBottomSheet!!.dialog!!.dismiss()
                }

            }, object : OnDialogDismiss {
                override fun onDismiss() {
                    showBottomSheet = false
                }

            })
        when {
            SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByDate()!! && !SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByASC()!! -> {
                mListSelectionBottomSheet!!.setSelectionPos(0)
            }
            SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByDate()!! && SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByASC()!! -> {
                mListSelectionBottomSheet!!.setSelectionPos(1)
            }
            SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortBySize()!! && !SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByASC()!! -> {
                mListSelectionBottomSheet!!.setSelectionPos(2)
            }
            SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortBySize()!! && SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByASC()!! -> {
                mListSelectionBottomSheet!!.setSelectionPos(3)
            }
            SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByName()!! && !SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByASC()!! -> {
                mListSelectionBottomSheet!!.setSelectionPos(4)
            }
            SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByName()!! && SharedPreferenceUtils.getInstance(requireContext())
                ?.getSortByASC()!! -> {
                mListSelectionBottomSheet!!.setSelectionPos(5)
            }
        }
        mListSelectionBottomSheet!!.show(
            requireActivity().supportFragmentManager,
            "bottomSheetAudioRecordMode"
        )
    }

    private fun startApplyNewSort() {
        viewModel.applyNewSort()
    }

    override fun onSectionHeaderClick() {

    }

    private fun showPopupItems(): PopupWindow {
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_popup_myfile, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rcPopup)
        val adapter = MyFilePopupAdapter(requireContext())
        adapter.addItemPopup(Utils.getPopupItems(requireContext()))
        recyclerView.adapter = adapter

        adapter.setOnClickListener(object : MyFilePopupAdapter.OnClickEvents {
            override fun onPopupClicked(position: Int) {
                when (position) {
                    0 -> {
                        closePopup()
                    }
                    1 -> {

                    }
                    2 -> {

                    }
                    3 -> {

                    }
                    4 -> {

                    }
                    5 -> {

                    }
                }
            }
        })

        return PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun openPopup(view: View) {
        popupWindow = showPopupItems()
        popupWindow?.isOutsideTouchable = true
        popupWindow?.isFocusable = true
        popupWindow?.showAsDropDown(view)
    }

    private fun closePopup() {
        popupWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            popupWindow = null
        }
    }
}