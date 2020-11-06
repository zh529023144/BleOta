package com.roche.ota.ui.update.ota

import com.roche.ota.base.IBaseView
import com.roche.ota.model.response.*

interface IOtaView : IBaseView {
    //后台处理指令
    fun onBleKeySucceed(response: BleCodeResponse)
    fun onBleKeyError(error: String, errorCode: Int)

    //蓝牙解锁
    fun onBleUnlockSucceed(response: OldKeyResponse)

    fun onBleUnlockError(error: String, errorCode: Int)

//    //判断设备是否已经入库
//    fun onIsDevPutSucceed(response: IsDevPutResponse)
//    fun onIsDevPutError(error: String, errorCode: Int)

    //升级日志
    fun onUpdateLogSucceed(response: BaseResponse)
    fun onUpdateLogError(error: String, errorCode: Int)


    //更新最新版本
    fun onUpdateVersionSucceed(response: BaseResponse)
    fun onUpdateVersionError(error: String, errorCode: Int)

}