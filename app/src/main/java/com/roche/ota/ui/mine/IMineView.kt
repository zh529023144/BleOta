package com.roche.ota.ui.mine

import com.roche.ota.base.IBaseView
import com.roche.ota.model.response.UserResponse

interface IMineView : IBaseView {
    fun onUserSucceed(response: UserResponse)

    fun onUserError(error: String, errorCode: Int)
}