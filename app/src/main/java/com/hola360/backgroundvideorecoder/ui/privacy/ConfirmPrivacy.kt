package com.hola360.backgroundvideorecoder.ui.privacy

import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.NavMainGraphDirections
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentConfirmPrivacyBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment

class ConfirmPrivacy: BaseFragment<FragmentConfirmPrivacyBinding>() {

    override val layoutId: Int = R.layout.fragment_confirm_privacy
    override val showToolbar: Boolean = false
    override val toolbarTitle: String? = null
    override val menuCode: Int = 0

    override fun initViewModel() {
    }

    override fun initView() {
        setupTextViewClick()
        binding!!.agree.setOnClickListener {
            dataPref!!.putBooleanValue(MainActivity.PRIVACY, true)
            findNavController().popBackStack()
        }

    }

    private fun setupTextViewClick(){
        val termClick= object :ClickableSpan(){
            override fun onClick(widget: View) {
                findNavController().navigate(NavMainGraphDirections.actionToNavPrivacy(false))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.colorLightAccent)
                ds.isUnderlineText = true
            }
        }
        val privacyClick= object :ClickableSpan(){
            override fun onClick(widget: View) {
                findNavController().navigate(NavMainGraphDirections.actionToNavPrivacy(true))
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.colorLightAccent)
                ds.isUnderlineText = true
            }
        }
        val string= resources.getString(R.string.privacy_policy_text)
        val strTerm= resources.getString(R.string.term_of_service)
        val strPrivacy= resources.getString(R.string.privacy_policy)
        val termStartIndex= string.indexOf(strTerm)
        val privacyStartIndex= string.indexOf(strPrivacy)
        val spannableString= SpannableStringBuilder(string)
        spannableString.setSpan(termClick, termStartIndex, termStartIndex+ strTerm.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(privacyClick, privacyStartIndex, privacyStartIndex+ strPrivacy.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding!!.privacyPolicy.text= spannableString
        binding!!.privacyPolicy.movementMethod= LinkMovementMethod.getInstance()
    }


}