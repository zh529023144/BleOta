package com.roche.ota.api

import com.roche.ota.model.response.*
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by xuhao on 2017/11/16.
 * Api 接口
 */

interface ApiService {


    //登录
    @POST("VendingSystem/login")
    fun login(@Query("username") name: String, @Query("password") pass: String): Observable<LoginResponse>


//    fun login(@Body body: RequestBody): Observable<BaseResponse<LoginData>>


    //获取设备详情接口
    @POST("")
    fun getDeviceDetail(@Body body: RequestBody): Observable<BaseResponse>

    // 入库接口
    @POST("VendingSystem/bindingDev/scanDev")
    fun putDevice(
        @Query("devId") devId: String,
        @Query("qrCode") qrCode: String,
        @Query("model") model: String,
        @Query("username") username: String,
        @Query("blueToothId") blueToothId: String

    ): Observable<DevKeyResponse>

    //初始化同步密钥
    @GET("VendingSystem/bindingDev/createInitialCode")
    fun getSynKey(
        @Query("btMac") btMac: String?,
        @Query("devId") devId: String?,
        @Query("bTCode") bTCode: String,
        @Query("useOrigin") useOrigin: Boolean
    ): Observable<BaseResponse>

    //同步后端
    @POST("VendingSystem/bindingDev/saveInitialCode")
    fun getSynService(
        @Query("devId") devId: String,
        @Query("version") version: String
    ): Observable<BaseResponse>


    //获取合伙人

    @POST("VendingSystem/factoryUser/findPartnerAll")
    fun getFactoryUser(
        @Query("status") status: String,
        @Query("finish") finish: String,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<FactoryUserResponse>


    //微信二维码获取设备id
    @GET("VendingSystem/wxc/qr/getDevIdByQrCode")
    fun getDevCode(
        @Query("code") code: String
    ): Observable<BaseResponse>


    //判断设备是否已经入库
    @GET("VendingSystem/bindingDev/existsByDev")
    fun isDevPut(
        @Query("devId") devId: String
    ): Observable<IsDevPutResponse>

    //判断设备是否已经入库
    @GET("VendingSystem/front/repair/existsByDevMac")
    fun isDevPutByMac(
        @Query("btMac") devId: String
    ): Observable<IsDevPutResponse>

    //获取机型

    @GET("VendingSystem/modelConfig/list")
    fun getDevModelList(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<DevModelResponse>

//    //二次同步秘钥  中途断开后  同步操作一次
//    @GET("VendingSystem/front/repair/getDevOldKey")
//    fun getSynDev(
//        @Query("devId") devId: String
//    ): Observable<OldKeyResponse>

    //转移 设备

    @PUT("VendingSystem/bindingDev/bindPartnerForApp")
    fun addBind(
        @Body body: RequestBody
    ): Observable<BaseResponse>


    //后台蓝牙指令解析
    @GET("VendingSystem/front/util/btRet")
    fun getBleKey(
        @Query("devId") devId: String,
        @Query("btRet") btRet: String

    ): Observable<BleCodeResponse>


    //密码 同步
    @GET("VendingSystem/bindingDev/getFeaturesCode")
    fun getFeaturesCode(
        @Query("btRet") btRet: String?,
        @Query("devId") devId: String?

    ): Observable<BaseResponse>


    //死指令处理
    @GET("VendingSystem/ins/fd")
    fun getLocalKey(
        @Query("devId") devId: String,
        @Query("insName") insName: String,
        @Query("param") param: String

    ): Observable<BaseResponse>

    //获取设备信息
    @GET("VendingSystem/front/dev/getModelConfig")
    fun getDev(
        @Query("model") devId: String
    ): Observable<DevConfigResponse>

    //蓝牙解锁
    @GET("VendingSystem/front/repair/getDevOldKey")
    fun getDevkey(
        @Query("useOrigin") useOrigin: Boolean,
        @Query("btMac") btMac: String
    ): Observable<OldKeyResponse>


    //升级日志上报

    @POST("VendingSystem/front/dev/saveUpdateLog")
    fun addUpdateLog(
        @Body body: RequestBody
    ): Observable<BaseResponse>

    //获取用户信息
    @GET("VendingSystem/user/info")
    fun getUserData(): Observable<UserResponse>

    //未绑定酒店的接口
    @GET("VendingSystem/version/unBindHotelList")
    fun getUnbindHotel(
    ): Observable<UnbindHotelResponse>

    //酒店列表接口
    @GET("VendingSystem/version/hotelUpgradeList")
    fun getBindHotel(
        @Query("hotelName") hotelName: String?,
        @Query("lastPartner") lastPartner: String?,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<BindHotelResponse>

    //列表详情接口
    @GET("VendingSystem/version/hotelDevDetail")
    fun getListDetail(
        @Query("status") status: String?,
        @Query("hotelId") hotelId: String?,
        @Query("roomCode") roomCode: String?,
        @Query("devId") devId: String?,
        @Query("blueToothId") blueToothId: String?,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Observable<HotelDetailResponse>

    //蓝牙版本升级文件接口
    @GET("VendingSystem/version/findInstallPackage")
    fun getUpdateConfig(
        @Query("model") model: String
    ): Observable<BleUpdateConfigResponse>

    //更新设备版本接口
    @POST("VendingSystem/version/updateDevVersion")
    fun getUpdateBleVersion(
        @Query("devId") devId: String?,
        @Query("blueToothId") blueToothId: String?,
        @Query("initBtCode") initBtCode: String
    ): Observable<BaseResponse>


    //查询是否可操作
    @GET("VendingSystem/version/checkIsUpgradeDev")
    fun getIsHasDev(
        @Query("devId") devId: String?,
        @Query("blueToothId") blueToothId: String?
    ): Observable<DevIsHasResponse>

    //查询该酒店下的机型详情

    @GET("VendingSystem/version/hotelDetail")
    fun getHotelModelDetail(
        @Query("hotelId") hotelId: String?
    ): Observable<HotelModelResponse>


}