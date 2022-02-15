package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.pickfolder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hola360.backgroundvideorecoder.databinding.ItemStoragePathBinding
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.utils.FilePickerUtils


class BrowserPathAdapter :
    RecyclerView.Adapter<BrowserPathAdapter.ViewHolder>() {
    val data = mutableListOf<String>()
    var onPathClickListener: OnPathClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemStoragePathBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    fun updatePaths(paths: MutableList<String>) {
//        if (data.isNotEmpty()) {
//            notifyItemRangeRemoved(0, data.size)
//        }
        data.clear()
        data.addAll(paths)
//        notifyItemInserted(0)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(private val binding: ItemStoragePathBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.myTextViewPath.text = data[position]
            binding.myTextViewPath.setOnClickListener {
                onPathClickListener?.onItemClick(position)
            }
            if (position == data.size - 1) {
                binding.myTextViewPath.setTextColor(FilePickerUtils.fetchAccentColor(binding.root.context))
            } else {
                binding.myTextViewPath.setTextColor(FilePickerUtils.fetchPrimaryTextColor(binding.root.context))
            }
            FilePickerUtils.updateSelectImage(binding.myImageViewIcon, position == data.size - 1)
        }
    }

    interface OnPathClickListener {
        fun onItemClick(position: Int)
    }
}