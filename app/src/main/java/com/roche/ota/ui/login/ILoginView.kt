package com.roche.ota.ui.login

import com.roche.ota.base.IBaseView
import com.roche.ota.model.response.BaseResponse
import com.roche.ota.model.response.LoginResponse

/**
 * 创建日期：2020/1/8 on 16:52
 * 描述:
 * 作者:张伦欢
 */

interface ILoginView : IBaseView {

    fun onLoginSucceed(response: LoginResponse)

    fun onLoginError(error: String, errorCode: Int)
}



