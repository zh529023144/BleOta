package com.roche.ota.ui.statistic

import com.roche.ota.base.IBaseView
import com.roche.ota.model.response.BindHotelResponse
import com.roche.ota.model.response.HotelListResponse
import com.roche.ota.model.response.HotelModelResponse
import com.roche.ota.model.response.UnbindHotelResponse

interface IStatisticView : IBaseView {
    //已绑定酒店列表
    fun onGetBindHotelSucceed(response: BindHotelResponse)

    fun onGetBindHotelError(error: String, errorCode: Int)

    //未绑定
    fun onGetUnbindHotelSucceed(response: UnbindHotelResponse)

    fun onGetUnbindHotelError(error: String, errorCode: Int)

    //列表酒店设备详情

    fun onGetHotelModelDetailSucceed(response: HotelModelResponse)
    fun onGetHotelModelDetailError(error: String, errorCode: Int)
}