package com.roche.ota.ui.statistic

import com.roche.ota.base.BasePresenter
import com.roche.ota.net.exception.ExceptionHandle

class StatisticPresenter : BasePresenter<IStatisticView>() {

    private val mModel by lazy {
        StatisticModel()
    }

    //酒店列表
    fun getBindHotel(hotelName: String?, lastPartner:String?,pageNumber: Int, pageSize: Int) {
        checkVIewAttached()

        val disposable =
            mModel.getBindHotel(hotelName, lastPartner,pageNumber, pageSize)
                .subscribe({
                    if (it.code == 200) {
                        mView.onGetBindHotelSucceed(it)
                    } else {
                        mView.onGetBindHotelError(
                            it.message,
                            it.code

                        )
                    }


                }, {
                    mView.dismissLoading()
                    mView.onGetBindHotelError(
                        ExceptionHandle.handleException(it),
                        ExceptionHandle.errorCode

                    )
                }, {
                    mView.dismissLoading()
                })

        addDisposable(disposable)
    }

    //
    fun getUnbindHotel() {
        checkVIewAttached()

        val disposable =
            mModel.getUnbindHotel()
                .subscribe({
                    if (it.code == 200) {
                        mView.onGetUnbindHotelSucceed(it)
                    } else {
                        mView.onGetUnbindHotelError(
                            it.message,
                            it.code

                        )
                    }


                }, {
                    mView.dismissLoading()
                    mView.onGetUnbindHotelError(
                        ExceptionHandle.handleException(it),
                        ExceptionHandle.errorCode

                    )
                }, {
                    mView.dismissLoading()
                })

        addDisposable(disposable)
    }
}