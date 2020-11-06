package com.roche.ota.utils

import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.roche.ota.R
import com.roche.ota.view.LoadingDialog

object LoadingHelper {
    private var loadingDialog: LoadingDialog? = null
    fun show(activity: Activity,tip:String) {
        activity as FragmentActivity
        if (loadingDialog != null && loadingDialog!!.isShowing && loadingDialog!!.ownerActivity === activity) {
            return
        }
        val view = View.inflate(activity, R.layout.dialog_waiting, null)
        if (!TextUtils.isEmpty(tip))
            (view.findViewById(R.id.tvTip) as TextView).text = tip
        loadingDialog = LoadingDialog(activity, view, R.style.MyDialog)


        loadingDialog!!.ownerActivity = activity
        loadingDialog!!.show()
    }


    private fun dismissLoading() {
        if (loadingDialog == null) {
            return
        }
        try {
            if (loadingDialog!!.isShowing) {
                loadingDialog!!.dismiss()
            }
        } catch (e: Exception) {
        } finally {
            loadingDialog = null
        }
    }

    fun cancel() {
        dismissLoading()
    }
}