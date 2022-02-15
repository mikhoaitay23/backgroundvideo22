package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.pickfolder.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hola360.backgroundvideorecoder.databinding.ItemFileInPickBinding
import com.hola360.backgroundvideorecoder.utils.BindingUtils.bindThumbnailFile
import java.io.File

class FileAdapter(
    private val listener: ListenerClickItem
) :
    RecyclerView.Adapter<FileAdapter.FileInPickViewHolder>() {
    val filteredList = mutableListOf<File>()
    fun update(newList: MutableList<File>?) {
        if (filteredList.isNotEmpty()) {
            notifyItemRangeRemoved(0, filteredList.size)
        }
        filteredList.clear()
        if (!newList.isNullOrEmpty()) {
            filteredList.addAll(newList)
        }
        notifyItemInserted(0)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileInPickViewHolder {
        val itemBinding = ItemFileInPickBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FileInPickViewHolder(itemBinding)


    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: FileInPickViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class FileInPickViewHolder(itemView: ItemFileInPickBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private val binding = itemView
        fun bind(position: Int) {
            binding.imgThumbnail.bindThumbnailFile(filteredList[position])
            binding.myTextViewTitle.text = filteredList[position].name
            binding.root.setOnClickListener {
                if (position < itemCount) {
                    listener.onClickFileItem(
                        position,
                        filteredList[position]
                    )
                }
            }
        }
    }

    interface ListenerClickItem {
        fun onClickFileItem(position: Int, cFile: File)

    }

}
