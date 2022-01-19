package com.hola360.backgroundvideorecoder.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.hola360.backgroundvideorecoder.databinding.LayoutToolbarBinding

class Toolbar: FrameLayout {
    private var binding: LayoutToolbarBinding?= null
    private var callback:CustomToolbarCallback?=null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        init()
    }

    private fun init(){
        binding= LayoutToolbarBinding.inflate(LayoutInflater.from(context), this, true)
        binding!!.menuCode=0
        binding!!.back.setOnClickListener {
            callback!!.onBack()
        }
    }


    fun setupToolbarCallback(callback: CustomToolbarCallback){
        this.callback= callback
    }

    fun showToolbarMenu(menuCode:Int){
        binding!!.menuCode= menuCode
    }

    fun setToolbarTitle(title:String?){
        title?.let {
            binding!!.title.text= it
        }
    }

    interface CustomToolbarCallback{
        fun onBack()
    }
}