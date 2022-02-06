package com.hola360.backgroundvideorecoder.ui.dialog.listdialog

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hola360.backgroundvideorecoder.databinding.ItemListSelectionBinding
import com.hola360.backgroundvideorecoder.ui.base.baseviewholder.BaseViewHolder

class ListSelectionAdapter(val listItems: MutableList<String>, val callback: OnItemListSelection): RecyclerView.Adapter<BaseViewHolder>() {

    private var selectionPos=0

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectionPos(position: Int){
        selectionPos= position
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding= ItemListSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemListSelectionHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    inner class ItemListSelectionHolder(val binding:ItemListSelectionBinding): BaseViewHolder(binding.root){
        @SuppressLint("NotifyDataSetChanged")
        override fun onBind(position: Int) {
            binding.check.isChecked= position==selectionPos
            binding.itemName.text= listItems[position]
            binding.item.setOnClickListener {
                if(position==selectionPos){
                    return@setOnClickListener
                }
                selectionPos=position
                notifyDataSetChanged()
                callback.onSelection(position)
            }
        }
    }

    interface OnItemListSelection{
        fun onSelection(position: Int)
    }
}