package com.roche.ota.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.roche.ota.R
import com.roche.ota.view.MultipleStatusView
import com.roche.ota.view.WaitingDialog



abstract class BaseFragment<P : BasePresenter<out IBaseView>?> : Fragment() {
    abstract val mPresenter: P
    protected val TAG = this.javaClass.simpleName
    private var isViewPrepare = false
    private var haLoadData = false
    private var mDialogWaiting: WaitingDialog? = null

    protected var mLayoutStatusView: MultipleStatusView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mPresenter?.let {
            lifecycle.addObserver(it as LifecycleObserver)
        }
        return inflater.inflate(layoutId(), null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewPrepare = true
        initView()
        lazyLoadData()

        mLayoutStatusView?.setOnClickListener { }
    }

    open val mRetryClickListener: View.OnClickListener = View.OnClickListener {
        lazyLoadData()
    }

    abstract fun lazyLoadData()

    abstract fun initView()

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {

        }
    }

    abstract fun layoutId(): Int


    /**
     * 显示等待提示框
     */
    fun showWaitingDialog(tip: String): Dialog {
        hideWaitingDialog()
        val view = View.inflate(activity, R.layout.dialog_waiting, null)
        if (!TextUtils.isEmpty(tip))
            (view.findViewById(R.id.tvTip) as TextView).text = tip
        mDialogWaiting = WaitingDialog(activity as Context, view, R.style.MyDialog)
        mDialogWaiting?.show()
        mDialogWaiting?.setCanceledOnTouchOutside(false)
//        mDialogWaiting?.setCancelable(false)
        return mDialogWaiting as WaitingDialog
    }

    fun hideWaitingDialog() {
        mDialogWaiting?.dismiss()
    }
}