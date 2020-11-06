package com.roche.ota.base

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import com.roche.ota.R
import com.roche.ota.view.WaitingDialog

abstract class BaseActivity<P : BasePresenter<out IBaseView>?> : AppCompatActivity() {
    protected val TAG = this.javaClass.simpleName
    abstract val mPresenter: P
    private var mDialogWaiting: WaitingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        mPresenter?.let {
            lifecycle.addObserver(it as LifecycleObserver)
        }
        initView()
        initData()
    }


    abstract fun layoutId(): Int

    abstract fun initView()

    abstract fun initData()

    inline fun <reified T> goToActivity() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

    fun openKeyBoard(mEditText: EditText, mContext: Context) {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun closeKeyBoard(mEditText: EditText, mContext: Context) {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mEditText.windowToken, 0)
    }



    /**
     * 显示等待提示框
     */
    fun showWaitingDialog(tip: String): Dialog {
        hideWaitingDialog()
        val view = View.inflate(this, R.layout.dialog_waiting, null)
        if (!TextUtils.isEmpty(tip))
            (view.findViewById(R.id.tvTip) as TextView).text = tip
        mDialogWaiting = WaitingDialog(this, view, R.style.MyDialog)
        mDialogWaiting?.show()
        mDialogWaiting?.setCanceledOnTouchOutside(false)
//        mDialogWaiting?.setCancelable(false)
        return mDialogWaiting as WaitingDialog
    }

    fun hideWaitingDialog() {
        mDialogWaiting?.dismiss()
    }
}