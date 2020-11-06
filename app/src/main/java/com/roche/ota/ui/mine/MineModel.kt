package com.roche.ota.ui.mine

import com.roche.ota.base.BaseModel
import com.roche.ota.model.response.UserResponse
import io.reactivex.Observable

class MineModel : BaseModel(){

    fun getUser(): Observable<UserResponse> {
        return RetrofitManager.service.getUserData()
            .compose(SchedulerUtils.ioToMain())
    }
}