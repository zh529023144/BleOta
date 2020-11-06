package com.roche.ota.ui.update.ota

import com.roche.ota.base.BaseModel
import com.roche.ota.model.request.UpdateRequest
import com.roche.ota.model.response.*
import io.reactivex.Observable

class OtaModel : BaseModel(){

    //后台处理指令
    fun getBleKey(devId: String,btRet: String): Observable<BleCodeResponse> {
        return RetrofitManager.service.getBleKey(devId,btRet)
            .compose(SchedulerUtils.ioToMain())
    }

    //蓝牙解锁

    fun getBleUnlock(devId: String): Observable<OldKeyResponse> {
        return RetrofitManager.service.getDevkey(false,devId)
            .compose(SchedulerUtils.ioToMain())
    }

    //判断设备是否已经入库
    fun isDevPut(devId: String): Observable<IsDevPutResponse>{
        return RetrofitManager.service.isDevPutByMac(devId)
            .compose(SchedulerUtils.ioToMain())
    }

    //上报升级日志
    fun upDateLog(request: UpdateRequest): Observable<BaseResponse>{
        return RetrofitManager.service.addUpdateLog(getRequestBody(request))
            .compose(SchedulerUtils.ioToMain())
    }

    //更新蓝牙版本接口
    fun upDateVersion(devId: String?,blueToothId:String?,btCode:String):Observable<BaseResponse>{
        return RetrofitManager.service.getUpdateBleVersion(devId,blueToothId,btCode)
            .compose(SchedulerUtils.ioToMain())
    }
}