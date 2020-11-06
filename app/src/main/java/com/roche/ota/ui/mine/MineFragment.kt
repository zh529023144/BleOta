package com.roche.ota.ui.mine

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.roche.ota.R
import com.roche.ota.base.BaseFragment
import com.roche.ota.model.response.UserResponse
import com.roche.ota.ui.mine.about.AboutActivity
import com.roche.ota.ui.mine.web.WebActivity
import kotlinx.android.synthetic.main.fragment_mine.*
import kotlin.system.exitProcess


/**
 * @author madreain
 * @date
 * module：
 * description：
 */

class MineFragment : BaseFragment<MinePresenter>(), IMineView {

    override fun layoutId(): Int {
        return R.layout.fragment_mine
    }

    override val mPresenter: MinePresenter by lazy {
        MinePresenter().apply {
            attachView(this@MineFragment)
        }
    }

    override fun initView() {

        Log.e(TAG, "initView")
        //加入我们
        ll_join.setOnClickListener {
            val intent = Intent(activity, WebActivity::class.java)
            startActivity(intent)
        }

        //关于
        ll_about.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java)
            startActivity(intent)
        }

        //退出APP
        ll_close.setOnClickListener {

            val builder = AlertDialog.Builder(activity)
            builder.setTitle("尊敬的用户")
            builder.setMessage("你真的要退出吗？")
            builder.setPositiveButton("残忍退出") { dialog, which -> exitProcess(0) }
            builder.setNegativeButton("我再想想") { dialog, which -> }
            val alert = builder.create()
            alert.show()


        }


    }

    override fun lazyLoadData() {
        Log.e(TAG, "lazyLoadData")
        mPresenter.getUser()
    }

    override fun showLoading() {
//        showWaitingDialog(getString(R.string.loading_text))
    }

    override fun dismissLoading() {
//        hideWaitingDialog()
    }

    companion object {
        fun newInstance(): MineFragment {//调用这个函数，创建新的fragment

            val fragment = MineFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onUserSucceed(response: UserResponse) {
        tv_me_name.text = response.result.username
        tv_me_phone.text = response.result.mobile
    }

    override fun onUserError(error: String, errorCode: Int) {
    }

}
