package com.hola360.backgroundvideorecoder.ui.dialog.listdialog

import androidx.recyclerview.widget.LinearLayoutManager
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.LayoutListSelectionDialogBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog
import com.hola360.backgroundvideorecoder.ui.dialog.OnDialogDismiss

class ListSelectionBotDialog(val title:String, private val listItem:MutableList<String>,
                             val callback: ListSelectionAdapter.OnItemListSelection,
                            val dismissCallback:OnDialogDismiss): BaseBottomSheetDialog<LayoutListSelectionDialogBinding>() {

    override fun getLayout(): Int {
        return R.layout.layout_list_selection_dialog
    }
    private val adapter: ListSelectionAdapter by lazy {
        ListSelectionAdapter(listItem, callback)
    }
    private var selectionPos= 0

    fun setSelectionPos(position:Int){
        selectionPos= position
    }

    override fun initView() {
        binding!!.title.text= title
        binding!!.itemRv.setHasFixedSize(true)
        binding!!.itemRv.layoutManager= LinearLayoutManager(requireContext())
        binding!!.itemRv.adapter= adapter
    }

    override fun onResume() {
        super.onResume()
        adapter.setSelectionPos(selectionPos)
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissCallback.onDismiss()
    }

}