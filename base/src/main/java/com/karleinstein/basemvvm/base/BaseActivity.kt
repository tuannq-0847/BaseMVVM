package com.karleinstein.basemvvm.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

abstract class BaseActivity : AppCompatActivity(), BaseView {

    override val viewModel: BaseViewModel? = null
    private var loadingAndErrorListener: LoadingAndErrorListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        setUpView()
        bindView()
        if (viewModel == null) Log.d(
            "BaseActivity :",
            "${this::class.simpleName} viewModel is null"
        )
        else {
            viewModel!!.loadingEvent.observe(this, {
                loadingAndErrorListener?.onHandleShowLoading(it)

            })
            viewModel!!.errorEvent.observe(this, {
                loadingAndErrorListener?.onHandleError(it)

            })
        }
    }

    internal fun setOnLoadingAndErrorListener(loadingAndErrorListener: LoadingAndErrorListener) {
        this.loadingAndErrorListener = loadingAndErrorListener
    }

    abstract fun setUpView()

    abstract fun bindView()

    override fun onDestroy() {
        super.onDestroy()
        viewModelStore.clear()
    }

    companion object {

        const val DEFAULT_LAYOUT_RES = 1
    }
}
