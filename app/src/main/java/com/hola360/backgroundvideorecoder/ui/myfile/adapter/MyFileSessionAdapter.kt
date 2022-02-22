package com.hola360.backgroundvideorecoder.ui.myfile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anggrayudi.storage.file.mimeType
import com.bumptech.glide.Glide
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.ItemHeaderRcMyfileBinding
import com.hola360.backgroundvideorecoder.databinding.ItemRcMyfileBinding
import com.hola360.backgroundvideorecoder.utils.DateTimeUtils
import com.hola360.backgroundvideorecoder.utils.Utils
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import java.io.File

class MyFileSessionAdapter(
    private val title: String,
    private val files: MutableList<File>?,
    var onClickListener: OnClickListener
) : Section(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_rc_myfile)
        .headerResourceId(R.layout.item_header_rc_myfile)
        .build()
) {
    override fun getContentItemsTotal() = files?.size ?: 0

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
            if (files?.get(position)?.mimeType == binding.root.context.getString(R.string.audio_mime_type)) {
                binding.imgRecord.setImageResource(R.drawable.img_default_photo)
            } else {
                Glide.with(binding.imgRecord.context).load(files?.get(position)?.absolutePath)
                    .into(binding.imgRecord)
            }
            binding.tvName.text = files?.get(position)?.name
            binding.tvDuration.text = String.format(
                binding.root.context.getString(R.string.duration_size_my_file),
                Utils.getDuration(files?.get(position)!!),
                Utils.getFileSize(files[position])
            )
            binding.tvDateTime.text =
                DateTimeUtils.getTimeDateMyFile(files[position].lastModified())
            binding.btnOption.setOnClickListener {
                onClickListener.onOptionClicked(position, binding.btnOption)
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
        fun onOptionClicked(position: Int, view: View)
    }
}