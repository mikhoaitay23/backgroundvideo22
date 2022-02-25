package com.hola360.backgroundvideorecoder.ui.myfile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anggrayudi.storage.file.mimeType
import com.bumptech.glide.Glide
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.data.model.mediafile.MediaFile
import com.hola360.backgroundvideorecoder.databinding.ItemHeaderRcMyfileBinding
import com.hola360.backgroundvideorecoder.databinding.ItemRcMyfileBinding
import com.hola360.backgroundvideorecoder.utils.DateTimeUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class MyFileSectionAdapter(
    private val title: String,
    private val files: MutableList<MediaFile>,
    var onClickListener: OnClickListener
) : Section(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_rc_myfile)
        .headerResourceId(R.layout.item_header_rc_myfile)
        .build()
) {

    var isSelectMode = false

    fun updateSelected(position: Int) {
        files[position].isSelect = !files[position].isSelect
    }

    fun countItemSelected(): Int {
        return files.count { it.isSelect }
    }

    fun updateSelect(isAll: Boolean) {
        if (isAll) {
            files.forEachIndexed { index, mediaFile ->
                mediaFile.isSelect = true
            }
        } else {
            files.forEachIndexed { index, mediaFile ->
                mediaFile.isSelect = false
            }
        }
    }

    override fun getContentItemsTotal() = files.size

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        val itemRcMyFileBinding =
            ItemRcMyfileBinding.inflate(
                LayoutInflater.from(view!!.context),
                view as ViewGroup,
                false
            )
        return MediaViewHolder(itemRcMyFileBinding)
    }

    override fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder {
        val itemHeaderRcMyFileBinding = ItemHeaderRcMyfileBinding.inflate(
            LayoutInflater.from(view!!.context),
            view as ViewGroup,
            false
        )
        return HeaderViewHolder(itemHeaderRcMyFileBinding)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as MediaViewHolder).bind(position)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        (holder as HeaderViewHolder).bind()
    }

    inner class MediaViewHolder(val binding: ItemRcMyfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            if (isSelectMode) {
                binding.btnOption.visibility = View.INVISIBLE
                binding.btnSelect.visibility = View.VISIBLE
            } else {
                binding.btnOption.visibility = View.VISIBLE
                binding.btnSelect.visibility = View.INVISIBLE
            }
            if (files[position].file.mimeType == binding.root.context.getString(R.string.audio_mime_type)) {
                binding.imgRecord.setImageResource(R.drawable.img_default_photo)
            } else {
                Glide.with(binding.imgRecord.context).load(files[position].file.absolutePath)
                    .into(binding.imgRecord)
            }
            binding.tvName.text = files[position].file.name
            binding.tvDuration.text = String.format(
                binding.root.context.getString(R.string.duration_size_my_file),
                Utils.getDuration(files[position].file),
                Utils.getFileSize(files[position].file)
            )
            binding.tvDateTime.text =
                DateTimeUtils.getTimeDateMyFile(files[position].file.lastModified())
            binding.btnSelect.isChecked = files[position].isSelect

            binding.btnOption.setOnClickListener {
                onClickListener.onItemClicked(position, binding.btnOption)
            }
            binding.btnSelect.setOnClickListener {
                onClickListener.onItemClicked(position, binding.btnSelect)
            }
            binding.root.setOnClickListener {
                onClickListener.onItemClicked(position, binding.mLayoutRoot)
            }
        }
    }

    inner class HeaderViewHolder(val binding: ItemHeaderRcMyfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvDate.text = title
        }
    }

    interface OnClickListener {
        fun onSectionHeaderClick()
        fun onItemClicked(position: Int, view: View)
    }
}