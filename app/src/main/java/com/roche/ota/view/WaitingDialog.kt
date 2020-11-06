package com.roche.ota.view

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View

/**
 * 创建日期：2020/1/15 on 10:01
 * 描述:
 * 作者:张伦欢
 */
class WaitingDialog : Dialog {
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

    /**
     * 宽高由该方法的参数设置
     */
    constructor(
        context: Context, width: Int, height: Int, layout: View,
        style: Int
    ) : super(context, style) {
        // 设置内容
        setContentView(layout)
        // 设置窗口属性
        val window = window
        val params = window!!.attributes
        // 设置宽度、高度、密度、对齐方式
        val density = getDensity(context)
        params.width = (width * density).toInt()
        params.height = (height * density).toInt()
        params.gravity = Gravity.CENTER
        window.attributes = params

    }

    /**
     * 获取显示密度
     *
     * @param context
     * @return
     */
    fun getDensity(context: Context): Float {
        val res = context.resources
        val dm = res.displayMetrics
        return dm.density
    }

}