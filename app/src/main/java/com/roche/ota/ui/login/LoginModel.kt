package com.roche.ota.ui.login

import RetrofitManager
import com.roche.ota.base.BaseModel
import com.roche.ota.model.response.BaseResponse
import com.roche.ota.model.response.LoginResponse
import io.reactivex.Observable

/**
 * 创建日期：2020/1/8 on 16:57
 * 描述:
 * 作者:张伦欢
 */
class LoginModel : BaseModel() {


    fun login(name: String,pass:String): Observable<LoginResponse> {
        return RetrofitManager.service.login(name,pass)
            .compose(SchedulerUtils.ioToMain())
    }
}