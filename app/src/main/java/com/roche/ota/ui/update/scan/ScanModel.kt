package com.roche.ota.ui.update.scan

import com.roche.ota.base.BaseModel
import com.roche.ota.model.response.BaseResponse
import com.roche.ota.model.response.BleUpdateConfigResponse
import com.roche.ota.model.response.DevIsHasResponse
import com.roche.ota.model.response.HotelDetailResponse
import io.reactivex.Observable

class ScanModel : BaseModel(){

//    //获取设备详情
//    fun getDevDetail(status:String?,hotelId:String?,roomCode:String?,devId:String?,blueToothId:String?,pageNumber: Int,pageSize: Int):Observable<HotelDetailResponse>{
//        return RetrofitManager.service.getListDetail(status,hotelId,roomCode,devId,blueToothId,pageNumber,pageSize)
//            .compose(SchedulerUtils.ioToMain())
//    }

    //获取版本升级配置
    fun getUpdateConfig(): Observable<BleUpdateConfigResponse> {
        return RetrofitManager.service.getUpdateConfig()
            .compose(SchedulerUtils.ioToMain())
    }

    //查询设备是否可操作
    fun getIsHasDev(devId: String?,btRet: String?): Observable<DevIsHasResponse>{
        return RetrofitManager.service.getIsHasDev(devId,btRet)
            .compose(SchedulerUtils.ioToMain())
    }
}