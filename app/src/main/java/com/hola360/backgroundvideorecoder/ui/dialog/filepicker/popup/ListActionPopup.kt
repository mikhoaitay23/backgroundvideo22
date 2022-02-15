package com.hola360.backgroundvideorecoder.ui.dialog.filepicker.popup

import android.content.Context
import android.view.View
import android.widget.ListPopupWindow
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.ui.dialog.filepicker.model.ActionModel


class ListActionPopup(private val context: Context) {
    private val popupMenu: ListPopupWindow = ListPopupWindow(context)
    fun showPopup(
        anchor: View,
        array: MutableList<ActionModel>,
        onActionClickListener: ActionAdapter.OnActionClickListener?
    ) {
        val adapter = ActionAdapter(array, object : ActionAdapter.OnActionClickListener {
            override fun onItemActionClick(position: Int) {
                popupMenu.dismiss()
                onActionClickListener?.onItemActionClick(position)
            }
        })
        popupMenu.setAdapter(adapter)
        popupMenu.width = context.resources.getDimension(R.dimen.item_album_width).toInt()
        popupMenu.anchorView = anchor
        popupMenu.show()
    }
    fun isShowing(): Boolean {
        return popupMenu.isShowing
    }

    fun dismiss(){
        popupMenu.dismiss()
    }
}