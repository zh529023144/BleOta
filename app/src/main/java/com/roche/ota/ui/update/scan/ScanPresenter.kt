package com.roche.ota.ui.update.scan

import com.roche.ota.base.BasePresenter
import com.roche.ota.net.exception.ExceptionHandle

class ScanPresenter : BasePresenter<IScanView>(){

    private val mModel by lazy {
        ScanModel()
    }

    fun getVersionConfig(model: String){
        checkVIewAttached()

        val disposable = mModel.getUpdateConfig(model)
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

    //查询设备是否可操作
     fun getIsHasDev(devId: String?,btRet: String) {
        checkVIewAttached()

        val disposable = mModel.getIsHasDev(devId,btRet)
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
//                mView.dismissLoading()
                mView.onGetIsHasDevDevError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
//                mView.dismissLoading()
            })

        addDisposable(disposable)
    }
}