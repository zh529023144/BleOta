package com.roche.ota.ui.statistic

import com.roche.ota.base.BaseModel
import com.roche.ota.model.response.BindHotelResponse
import com.roche.ota.model.response.UnbindHotelResponse
import io.reactivex.Observable

class StatisticModel : BaseModel(){

    //酒店列表

    fun getBindHotel(hotelName:String?,lastPartner:String?,pageNumber: Int,pageSize: Int): Observable<BindHotelResponse> {
        return RetrofitManager.service.getBindHotel(hotelName,lastPartner,pageNumber,pageSize)
            .compose(SchedulerUtils.ioToMain())
    }


    //未绑定

    fun getUnbindHotel():Observable<UnbindHotelResponse>{
        return RetrofitManager.service.getUnbindHotel()
            .compose(SchedulerUtils.ioToMain())
    }
}