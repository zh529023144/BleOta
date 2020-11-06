package com.roche.ota.ui.statistic.detail

import com.roche.ota.base.BasePresenter
import com.roche.ota.net.exception.ExceptionHandle

class StatisticDetailPresenter : BasePresenter<IStatisticDetailView>(){

    private val mModel by lazy {
        StatisticDetailModel()
    }

    fun getDevDetail(status:String?,hotelId:String?,roomCode:String?,devId:String?,blueToothId:String?,pageNumber: Int,pageSize: Int){
        checkVIewAttached()
        val disposable = mModel.getDevDetail(status, hotelId, roomCode, devId, blueToothId, pageNumber, pageSize)
            .subscribe({
                if (it.code == 200) {
                    mView.onGetHotelDetailSucceed(it)
                } else {
                    mView.onGetHotelDetailError(
                        it.message,
                        it.code

                    )
                }

            }, {
                mView.dismissLoading()
                mView.onGetHotelDetailError(
                    ExceptionHandle.handleException(it),
                    ExceptionHandle.errorCode

                )
            }, {
                mView.dismissLoading()
            })

        addDisposable(disposable)
    }
}