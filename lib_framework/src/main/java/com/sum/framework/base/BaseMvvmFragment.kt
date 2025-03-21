package com.sum.framework.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.ParameterizedType

/**
 * @author mingyan.su
 * @date   2023/2/27 12:31
 * @desc   DataBinding和ViewModel基类
 */
abstract class BaseMvvmFragment<DB : ViewDataBinding, VM : ViewModel> : BaseDataBindFragment<DB>() {
    lateinit var mViewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    open fun initViewModel() {

        val argument = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
        //genericSuperclass 返回当前类的直接超类的类型，包括泛型信息，如BaseDataBindFragment<MyViewBinding>
        //actualTypeArguments 返回当前类的实际的泛型参数类型
        mViewModel = ViewModelProvider(this).get(argument[1] as Class<VM>)
    }

    }