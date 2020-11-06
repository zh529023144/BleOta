package com.roche.ota.ui.update

import RetrofitManager
import SchedulerUtils
import com.roche.ota.base.BaseModel
import com.roche.ota.model.request.BindPartnerRequest
import com.roche.ota.model.response.*
import io.reactivex.Observable

class UpdateModel : BaseModel(){
    fun putDevice(devId: String,qrCode: String,model: String,username: String,blueToothId: String): Observable<DevKeyResponse>{
       return RetrofitManager.service.putDevice(devId,qrCode,model,username,blueToothId)
            .compose(SchedulerUtils.ioToMain())
    }

    //获取密钥
    fun getSynKey(devId: String,bTCode: String): Observable<BaseResponse>{
        return RetrofitManager.service.getSynKey(devId,bTCode,false)
            .compose(SchedulerUtils.ioToMain())
    }

    //同步后台
    fun getSynService(devId: String,version: String): Observable<BaseResponse>{
        return RetrofitManager.service.getSynService(devId,version)
            .compose(SchedulerUtils.ioToMain())
    }

    //获取同伙人
    fun getFactoryUser(status: String,finish: String,pageNumber: Int,pageSize: Int): Observable<FactoryUserResponse>{
        return RetrofitManager.service.getFactoryUser(status,finish,pageNumber,pageSize)
            .compose(SchedulerUtils.ioToMain())
    }

    //获取机型
    fun getDevModelList(pageNumber: Int,pageSize: Int): Observable<DevModelResponse>{
        return RetrofitManager.service.getDevModelList(pageNumber,pageSize)
            .compose(SchedulerUtils.ioToMain())
    }


    //微信二维码获取设备id
    fun getDevCode(code: String): Observable<BaseResponse>{
        return RetrofitManager.service.getDevCode(code)
            .compose(SchedulerUtils.ioToMain())
    }

    //判断设备是否已经入库
    fun isDevPut(devId: String): Observable<IsDevPutResponse>{
        return RetrofitManager.service.isDevPut(devId)
            .compose(SchedulerUtils.ioToMain())
    }

    //转移合伙人
    fun addBind(request: BindPartnerRequest): Observable<BaseResponse>{
        return RetrofitManager.service.addBind(getRequestBody(request))
            .compose(SchedulerUtils.ioToMain())
    }

    //后台处理指令
    fun getBleKey(devId: String,btRet: String): Observable<BleCodeResponse>{
        return RetrofitManager.service.getBleKey(devId,btRet)
            .compose(SchedulerUtils.ioToMain())
    }

    //密码设备同步
    fun getFeaturesCode(code: String): Observable<BaseResponse>{
        return RetrofitManager.service.getFeaturesCode(code)
            .compose(SchedulerUtils.ioToMain())
    }

    //获取设备信息
    fun getDev(code: String): Observable<DevConfigResponse>{
        return RetrofitManager.service.getDev(code)
            .compose(SchedulerUtils.ioToMain())
    }

//    //获取设备详情
//    fun getDevDetail(status:String?,hotelId:String?,roomCode:String?,devId:String?,blueToothId:String?,pageNumber: Int,pageSize: Int):Observable<HotelDetailResponse>{
//        return RetrofitManager.service.getListDetail(status,hotelId,roomCode,devId,blueToothId,pageNumber,pageSize)
//            .compose(SchedulerUtils.ioToMain())
//    }

    //获取版本升级配置
    fun getUpdateConfig():Observable<BleUpdateConfigResponse>{
        return RetrofitManager.service.getUpdateConfig()
            .compose(SchedulerUtils.ioToMain())
    }

    //查询设备是否可操作
    fun getIsHasDev(devId: String?,btRet: String?): Observable<DevIsHasResponse>{
        return RetrofitManager.service.getIsHasDev(devId,btRet)
            .compose(SchedulerUtils.ioToMain())
    }

}