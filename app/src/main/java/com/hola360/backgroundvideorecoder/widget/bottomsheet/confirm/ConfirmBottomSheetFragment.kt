package com.hola360.backgroundvideorecoder.widget.bottomsheet.confirm

import android.os.Bundle
import android.os.Parcelable
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentBottomSheetConfirmBinding
import com.hola360.backgroundvideorecoder.ui.base.basedialog.BaseBottomSheetDialog
import kotlinx.android.parcel.Parcelize

class ConfirmBottomSheetFragment : BaseBottomSheetDialog<FragmentBottomSheetConfirmBinding>() {

    var onConfirmButtonClickListener: OnConfirmButtonClickListener? = null
    private var dataBuilder: DataBuilder? = null

    override fun initView() {
        dataBuilder = requireArguments().getParcelable(Data)
        if (dataBuilder != null) {
            if (dataBuilder!!.title != null) {
                binding!!.tvTitle.text = dataBuilder!!.title!!
            }
            if (dataBuilder!!.msg != null) {
                binding!!.tvMsg.text = dataBuilder!!.msg!!
            }
            if (dataBuilder!!.positive != null) {
                binding!!.btnPositive.text = dataBuilder!!.positive!!
            }
            if (dataBuilder!!.negative != null) {
                binding!!.btnNegative.text = dataBuilder!!.negative!!
            }
        }
        binding!!.btnPositive.setOnClickListener {
            onConfirmButtonClickListener?.onPositiveClick()
            dismiss()
        }
        binding!!.btnNegative.setOnClickListener {
            onConfirmButtonClickListener?.onNegativeClick()
            dismiss()
        }
    }

    override fun getLayout() = R.layout.fragment_bottom_sheet_confirm

    companion object {
        private const val Data = "data"
        fun create(builder: DataBuilder): ConfirmBottomSheetFragment {
            val selectionBottomSheetFragment = ConfirmBottomSheetFragment()
            val bundle = Bundle()
            bundle.putParcelable(Data, builder)
            selectionBottomSheetFragment.arguments = bundle
            return selectionBottomSheetFragment
        }
    }

    @Parcelize
    class DataBuilder : Parcelable {
        var title: String? = null
        var msg: String? = null
        var positive: String? = null
        var negative: String? = null

        fun addTitle(title: String): DataBuilder {
            this.title = title
            return this
        }

        fun addMsg(msg: String): DataBuilder {
            this.msg = msg
            return this
        }

        fun addPositive(positive: String): DataBuilder {
            this.positive = positive
            return this
        }

        fun addNegative(negative: String): DataBuilder {
            this.negative = negative
            return this
        }
    }

    interface OnConfirmButtonClickListener {
        fun onPositiveClick()
        fun onNegativeClick()
    }
}