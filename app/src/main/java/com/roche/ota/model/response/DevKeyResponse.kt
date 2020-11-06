package com.roche.ota.model.response

data class DevKeyResponse(
    val code: Int,
    val message: String,
    val result: Result,
    val success: Boolean,
    val timestamp: Long
)

data class Result(
    val initialCode: String,
    val noIntoDevCount: Int
)