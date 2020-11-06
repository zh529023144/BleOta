package com.roche.ota.ui.update

import com.roche.ota.base.IBaseView
import com.roche.ota.model.response.*

interface IUpdateView : IBaseView {

    //微信二维码获取设备id
    fun onGetDevCodeSucceed(response: BaseResponse)
    fun onGetDevCodeError(error: String, errorCode: Int)




    //获取更新配置
    fun onGetUpdateConfigSucceed(response: BleUpdateConfigResponse)
    fun onGetUpdateConfigError(error: String, errorCode: Int)

    //是否可操作
    fun onGetIsHasDevSucceed(response: DevIsHasResponse)
    fun onGetIsHasDevDevError(error: String, errorCode: Int)
}