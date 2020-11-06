package com.roche.ota.ui.statistic.detail

import com.roche.ota.base.BaseModel
import com.roche.ota.model.response.HotelDetailResponse
import io.reactivex.Observable

class StatisticDetailModel : BaseModel(){

    fun getDevDetail(status:String?,hotelId:String?,roomCode:String?,devId:String?,blueToothId:String?,pageNumber: Int,pageSize: Int):Observable<HotelDetailResponse>{
        return RetrofitManager.service.getListDetail(status,hotelId,roomCode,devId,blueToothId,pageNumber,pageSize)
            .compose(SchedulerUtils.ioToMain())
    }
}