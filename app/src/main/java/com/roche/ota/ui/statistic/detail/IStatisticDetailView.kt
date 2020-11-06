package com.roche.ota.ui.statistic.detail

import com.roche.ota.base.IBaseView
import com.roche.ota.model.response.HotelDetailResponse
import com.roche.ota.model.response.HotelListResponse

interface IStatisticDetailView : IBaseView {


    //列表详情
    fun onGetHotelDetailSucceed(response: HotelDetailResponse)

    fun onGetHotelDetailError(error: String, errorCode: Int)
}