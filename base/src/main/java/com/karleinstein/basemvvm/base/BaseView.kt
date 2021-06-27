package com.karleinstein.basemvvm.base

import androidx.viewbinding.ViewBinding

interface BaseView {

    val viewModel: BaseViewModel?

    val viewBinding: ViewBinding
}

interface BaseDialogView {

    val viewBinding: ViewBinding
}
