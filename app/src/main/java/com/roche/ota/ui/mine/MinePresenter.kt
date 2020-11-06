package com.roche.ota.ui.mine

import com.roche.ota.base.BasePresenter
import com.roche.ota.net.exception.ExceptionHandle

class MinePresenter : BasePresenter<IMineView>(){

    private val mMineModel by lazy {
        MineModel()
    }


    fun getUser() {
        checkVIewAttached()
        mView.showLoading()

        val disposable = mMineModel.getUser()
            .subscribe({
                if (it.code == 200) {
                    mView.onUserSucceed(it)
                } else {
                    mView.onUserError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onUserError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })
        addDisposable(disposable)
    }
}