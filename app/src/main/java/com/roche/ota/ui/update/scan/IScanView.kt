package com.roche.ota.ui.update.scan

import com.roche.ota.base.IBaseView
import com.roche.ota.model.response.BaseResponse
import com.roche.ota.model.response.BleUpdateConfigResponse
import com.roche.ota.model.response.DevIsHasResponse
import com.roche.ota.model.response.HotelDetailResponse

interface IScanView : IBaseView {
    //获取更新配置
    fun onGetUpdateConfigSucceed(response: BleUpdateConfigResponse)
    fun onGetUpdateConfigError(error: String, errorCode: Int)

    //是否可操作
    fun onGetIsHasDevSucceed(response: DevIsHasResponse)
    fun onGetIsHasDevDevError(error: String, errorCode: Int)
}