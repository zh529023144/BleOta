package com.roche.ota.view

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialog
import com.roche.ota.R

class LoadingDialog : AppCompatDialog {
    constructor(context: Context?) : super(context, R.style.MyDialog) {
        initView()
        initLayout()
    }

    constructor(
        context: Context?,
        @LayoutRes layoutResID: Int
    ) : super(context, R.style.loadingDialog) {
        initView()
        setContentView(layoutResID)
    }

    private fun initView() {
        setContentView(R.layout.dialog_waiting)

    }

    private fun initLayout() {
        val window = window

        val params = window!!.attributes

        params.gravity = Gravity.CENTER

        window.attributes = params
        setCancelable(false)
    }

    /**
     * 宽高由布局文件中指定（但是最底层的宽度无效，可以多嵌套一层解决）
     */
    constructor(context: Context, layout: View, style: Int) : super(context, style) {

        setContentView(layout)

        val window = window

        val params = window!!.attributes

        params.gravity = Gravity.CENTER

        window.attributes = params

    }
}