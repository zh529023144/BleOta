package com.roche.ota.model.request

/**
 * 创建日期：2020/1/8 on 17:05
 * 描述:
 * 作者:张伦欢
 */
data class LoginRequest(
    val loginName: String,
    val password: String,
    val loginType: String

)