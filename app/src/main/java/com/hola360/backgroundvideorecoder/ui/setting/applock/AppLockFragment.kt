package com.hola360.backgroundvideorecoder.ui.setting.applock

import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hola360.backgroundvideorecoder.MainActivity
import com.hola360.backgroundvideorecoder.R
import com.hola360.backgroundvideorecoder.databinding.FragmentAppLockBinding
import com.hola360.backgroundvideorecoder.ui.base.basefragment.BaseFragment

class AppLockFragment: BaseFragment<FragmentAppLockBinding>(), View.OnClickListener {

    override val layoutId: Int= R.layout.fragment_app_lock
    override val showToolbar: Boolean= false
    override val toolbarTitle: String= ""
    override val menuCode: Int= 0
    val type: Int by lazy {
        AppLockFragmentArgs.fromBundle(requireArguments()).type
    }
    private val passCode:String by lazy {
        dataPref!!.getPasscode() ?: ""
    }
    private var inputNumber:String=""
    private var inputCount:Int=0


    override fun initView() {
        binding!!.number= inputCount
        binding!!.mode= type
        setTitle()
        binding!!.one.setOnClickListener(this)
        binding!!.two.setOnClickListener(this)
        binding!!.three.setOnClickListener(this)
        binding!!.four.setOnClickListener(this)
        binding!!.five.setOnClickListener(this)
        binding!!.six.setOnClickListener(this)
        binding!!.seven.setOnClickListener(this)
        binding!!.eight.setOnClickListener(this)
        binding!!.nine.setOnClickListener(this)
        binding!!.zero.setOnClickListener(this)
        binding!!.delete.setOnClickListener {
            when(inputCount){
                in 1..4->{
                    inputCount--
                    inputNumber= inputNumber.substring(0, inputNumber.length-1)
                }
                5->{
                    inputCount= 0
                    inputNumber=""
                }
                in 6..9->{
                    inputCount--
                    inputNumber= inputNumber.substring(0, inputNumber.length-1)
                }
            }
            binding!!.number= inputCount
            setTitle()
        }
        binding!!.cancel.setOnClickListener{
            if(type== LOGIN_MODE){
                (requireActivity() as MainActivity).finish()
            }else{
                findNavController().popBackStack()
            }
        }
    }

    override fun initViewModel() {
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.one->{
                inputNumber += "1"
            }
            R.id.two->{
                inputNumber += "2"
            }
            R.id.three->{
                inputNumber += "3"
            }
            R.id.four->{
                inputNumber += "4"
            }
            R.id.five->{
                inputNumber += "5"
            }
            R.id.six->{
                inputNumber += "6"
            }
            R.id.seven->{
                inputNumber += "7"
            }
            R.id.eight->{
                inputNumber += "8"
            }
            R.id.nine->{
                inputNumber += "9"
            }
            R.id.zero->{
                inputNumber += "0"
            }
        }
        checkType()
    }

    private fun checkType(){
        inputCount++
        Log.d("abcVideo", "Input number add: $inputNumber ")
        if(inputCount==4){
            when(type){
                LOGIN_MODE->{
                    if(passCode == inputNumber){
                        findNavController().popBackStack()
                    }else{
                        inputCount=0
                        inputNumber=""
                        Snackbar.make(binding!!.root,
                            getString(R.string.app_lock_incorrect_passcode), Snackbar.LENGTH_SHORT).show()
                    }
                }
                CREATE_MODE->{
                    inputCount++
                    inputNumber+= "-"
                }
                DELETE_MODE->{
                    if(passCode == inputNumber){
                        dataPref!!.putPasscode("")
                        findNavController().popBackStack()
                    }else{
                        inputCount=0
                        inputNumber=""
                        Snackbar.make(binding!!.root,
                            getString(R.string.app_lock_incorrect_passcode), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }else if(inputCount==9){
            val passCodes= inputNumber.split("-").toMutableList()
            if(passCodes[0] == passCodes[1]){
                dataPref!!.putPasscode(passCodes[0])
                findNavController().popBackStack()
                Snackbar.make(binding!!.root,
                    getString(R.string.app_lock_set_passcode_success), Snackbar.LENGTH_SHORT).show()
            }else{
                inputCount=0
                inputNumber=""
                Snackbar.make(binding!!.root,
                    getString(R.string.app_lock_set_passcode_failed), Snackbar.LENGTH_SHORT).show()
            }
        }
        binding!!.number= inputCount
        setTitle()
    }

    private fun setTitle(){
        binding!!.title.text= when(type){
            LOGIN_MODE->{
                getString(R.string.app_lock_unlock_app)
            }
            CREATE_MODE->{
                if(inputCount<5){
                    getString(R.string.app_lock_create_passcode)
                }else{
                    getString(R.string.app_lock_confirm_passcode)
                }
            }
            else->{
                getString(R.string.app_lock_remove_passcode)
            }
        }
    }

    companion object{
        const val CREATE_MODE= 1
        const val LOGIN_MODE=2
        const val DELETE_MODE=3
    }

}