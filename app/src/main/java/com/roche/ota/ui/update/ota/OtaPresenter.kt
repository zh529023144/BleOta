package com.roche.ota.ui.update.ota

import com.roche.ota.base.BasePresenter
import com.roche.ota.model.request.UpdateRequest
import com.roche.ota.net.exception.ExceptionHandle

class OtaPresenter : BasePresenter<IOtaView>(){

    private val mOtaModel by lazy {
        OtaModel()
    }

    //后台处理蓝牙指令
    fun getBleKey(devId: String,btRet: String) {
        checkVIewAttached()

        val disposable = mOtaModel.getBleKey(devId,btRet)
            .subscribe({
                if (it.code == 200) {
                    mView.onBleKeySucceed(it)
                } else {
                    mView.onBleKeyError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onBleKeyError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })

        addDisposable(disposable)
    }

    //蓝牙解锁
    fun getBleUnlock(devId: String) {
        checkVIewAttached()

        val disposable = mOtaModel.getBleUnlock(devId)
            .subscribe({
                if (it.code == 200) {
                    mView.onBleUnlockSucceed(it)
                } else {
                    mView.onBleUnlockError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onBleUnlockError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })

        addDisposable(disposable)
    }

//    //判断设备是否已经入库
//    fun isDevPut(devId: String) {
//        checkVIewAttached()
//
//        val disposable = mOtaModel.isDevPut(devId)
//            .subscribe({
//                if (it.code == 200) {
//                    mView.onIsDevPutSucceed(it)
//                } else {
//                    mView.onIsDevPutError(
//                        it.message,
//                        it.code
//
//                    )
//                }
//
//
//            }, {
//                mView.dismissLoading()
//                mView.onIsDevPutError(
//                    ExceptionHandle.handleException(it),
//                    ExceptionHandle.errorCode
//
//                )
//            }, {
//                mView.dismissLoading()
//            })
//
//        addDisposable(disposable)
//    }

    //升级日志
    fun upDateLog(request: UpdateRequest) {
        checkVIewAttached()
        mView.showLoading()
        val disposable = mOtaModel.upDateLog(request)
            .subscribe({
                if (it.code == 200) {
                    mView.onUpdateLogSucceed(it)
                } else {
                    mView.onUpdateLogError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onUpdateLogError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })

        addDisposable(disposable)
    }


    fun  upDateVersion(devId: String?,blueToothId:String?,btCode:String){
        checkVIewAttached()

        val disposable = mOtaModel.upDateVersion(devId,blueToothId,btCode)
            .subscribe({
                if (it.code == 200) {
                    mView.onUpdateVersionSucceed(it)
                } else {
                    mView.onUpdateVersionError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onUpdateVersionError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })

        addDisposable(disposable)
    }
}