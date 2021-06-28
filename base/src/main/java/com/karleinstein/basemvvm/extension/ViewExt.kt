package com.karleinstein.basemvvm.extension

import android.view.View

fun View.OnClickListener.setOnClick(vararg view: View) {
    for (v in view) {
        v.setOnClickListener(this)
    }
}
