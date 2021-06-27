package com.karleinstein.basemvvm.base

import android.util.Log

interface LoadingAndErrorListener {

    fun onHandleShowLoading(isShowLoading: Boolean)

    fun onHandleError(throwable: Throwable)
}
