package com.roche.ota.model.response

/**
 * 创建日期：2020/1/8 on 17:05
 * 描述:
 * 作者:张伦欢
 */
data class BaseResponse(
    val code: Int,
    val message: String,
    val result: String,
    val success: Boolean,
    val timestamp: Long,
    var type: Int
)