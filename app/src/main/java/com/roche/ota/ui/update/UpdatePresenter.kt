package com.roche.ota.ui.update

import com.roche.ota.base.BasePresenter
import com.roche.ota.net.exception.ExceptionHandle

class UpdatePresenter : BasePresenter<IUpdateView>(){
    private val mWorkModel by lazy {
        UpdateModel()
    }


    //微信二维码获取设备id
    fun getDevCode(code: String,type :Int) {
        checkVIewAttached()

        val disposable = mWorkModel.getDevCode(code)
            .subscribe({
                it.type=type
                if (it.code == 200) {
                    mView.onGetDevCodeSucceed(it)
                } else {
                    mView.onGetDevCodeError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onGetDevCodeError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })

        addDisposable(disposable)
    }

    //查询设备是否可操作
    fun getIsHasDev(devId: String?,btRet: String?) {
        checkVIewAttached()

        val disposable = mWorkModel.getIsHasDev(devId,btRet)
            .subscribe({
                if (it.code == 200) {
                    mView.onGetIsHasDevSucceed(it)
                } else {
                    mView.onGetIsHasDevDevError(
                        it.message,
                        it.code

                    )
                }


            }, {
                mView.dismissLoading()
                mView.onGetIsHasDevDevError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })

        addDisposable(disposable)
    }

    fun getVersionConfig(model: String){
        checkVIewAttached()

        val disposable = mWorkModel.getUpdateConfig(model)
            .subscribe({
                if (it.code == 200) {
                    mView.onGetUpdateConfigSucceed(it)
                } else {
                    mView.onGetUpdateConfigError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onGetUpdateConfigError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })

        addDisposable(disposable)
    }

}