package com.roche.ota.ui.login

import com.roche.ota.base.BasePresenter
import com.roche.ota.net.exception.ExceptionHandle

/**
 * 创建日期：2020/1/8 on 16:56
 * 描述:
 * 作者:张伦欢
 */
class LoginPresenter : BasePresenter<ILoginView>() {

    private val mLoginModel by lazy {
        LoginModel()
    }


    fun login(name: String, pass: String) {
        checkVIewAttached()
        mView.showLoading()

        val disposable = mLoginModel.login(name, pass)
            .subscribe({
                if (it.code == 200) {
                    mView.onLoginSucceed(it)
                } else {
                    mView.onLoginError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onLoginError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })
        addDisposable(disposable)
    }
}