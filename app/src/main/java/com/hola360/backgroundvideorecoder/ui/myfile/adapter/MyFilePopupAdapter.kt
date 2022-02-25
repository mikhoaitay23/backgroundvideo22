package com.hola360.backgroundvideorecoder.ui.myfile.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hola360.backgroundvideorecoder.data.model.popup.PopupModel
import com.hola360.backgroundvideorecoder.databinding.ItemMyFilePopupBinding

class MyFilePopupAdapter(val context: Context) :
    RecyclerView.Adapter<MyFilePopupAdapter.PopupViewHolder>() {

    var itemPopupList: List<PopupModel> = mutableListOf()
    var onClickEvents: OnClickEvents? = null

    private var selectedItem: Int = -1

    fun addItemPopup(filers: List<PopupModel>) {
        itemPopupList = filers.toMutableList()
        notifyDataSetChanged()
    }

    fun selectedItem(position: Int) {
        selectedItem = position
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: PopupViewHolder, p1: Int) {
        holder.bind(p1)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PopupViewHolder {
        val itemMyFilePopupBinding = ItemMyFilePopupBinding.inflate(
            LayoutInflater.from(p0.context),
            p0,
            false
        )
        return PopupViewHolder(itemMyFilePopupBinding)
    }

    override fun getItemCount(): Int {
        return itemPopupList.size
    }

    inner class PopupViewHolder(val binding: ItemMyFilePopupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.mIcon.setImageResource(itemPopupList[position].icon)
            binding.mTitle.text = itemPopupList[position].name
            binding.root.setOnClickListener {
                onClickEvents?.onPopupClicked(position)
            }
        }
    }

    fun setOnClickListener(onClickEvents: OnClickEvents) {
        this.onClickEvents = onClickEvents
    }

    interface OnClickEvents {
        fun onPopupClicked(position: Int)
    }

}